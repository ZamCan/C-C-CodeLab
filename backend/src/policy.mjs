export class ApiError extends Error {
  constructor(status, code) { super(code); this.status = status; this.code = code; }
}
export const automaticStyles = new Set(['multiple_choice', 'true_false', 'output_prediction', 'error_identification', 'algorithm_selection']);
export function randomIndex(length) {
  if (!Number.isSafeInteger(length) || length < 1 || length > 0xffffffff) throw new Error('invalid_random_range');
  const ceiling = 0x100000000 - (0x100000000 % length);
  const bytes = new Uint32Array(1);
  do { crypto.getRandomValues(bytes); } while (bytes[0] >= ceiling);
  return bytes[0] % length;
}
export function validateQuestion(question) {
  // Reject malformed or ambiguous production items before an attempt is created.
  if (typeof question.prompt !== 'string' || question.prompt.trim().length < 12 ||
      question.prompt.length > 8000 || question.prompt !== question.prompt.trim())
    throw new ApiError(503, 'invalid_question_prompt');
  let choices;
  try { choices = JSON.parse(question.choices_json); }
  catch { throw new ApiError(503, 'invalid_question_choices'); }
  if (!Array.isArray(choices) || choices.length < 2 || choices.length > 8 ||
      choices.some(c => typeof c !== 'string' || c.trim().length === 0 || c.length > 2000 || c !== c.trim()) ||
      new Set(choices.map(c => c.normalize('NFC').toLocaleLowerCase('en-US'))).size !== choices.length ||
      !Number.isInteger(question.correct_index) || question.correct_index < 0 || question.correct_index >= choices.length)
    throw new ApiError(503, 'invalid_question');
  if (question.style === 'true_false' &&
      (choices.length !== 2 || new Set(choices.map(c => c.toLowerCase())).size !== 2 ||
       !choices.some(c => c.toLowerCase() === 'true') || !choices.some(c => c.toLowerCase() === 'false')))
    throw new ApiError(503, 'invalid_true_false_choices');
  return true;
}
export function auditQuestionBank(bank, slots) {
  if (!Array.isArray(bank) || !Array.isArray(slots)) throw new ApiError(503, 'invalid_question_bank');
  const failures = [], ids = new Set(), valid = [];
  for (const q of bank) {
    try {
      if (!q || typeof q.id !== 'string' || !/^[A-Za-z0-9_-]{1,100}$/.test(q.id) || ids.has(q.id))
        throw new ApiError(503, 'invalid_question_id');
      ids.add(q.id);
      validateQuestion(q);
      valid.push(q);
    } catch (error) {
      failures.push({id:q?.id ?? null,code:error.code ?? 'invalid_question'});
    }
  }
  const coverage = slots.map(slot => ({
    style:slot?.style, objective:slot?.objective, difficulty:slot?.difficulty,
    required:slot?.count,
    available:valid.filter(q => q.style===slot?.style && q.objective===slot?.objective && q.difficulty===slot?.difficulty).length
  }));
  const validBlueprint = slots.length>0 && slots.every(s=>s && automaticStyles.has(s.style) &&
    typeof s.objective==='string' && s.objective.length>0 &&
    ['easy','medium','hard'].includes(s.difficulty) && Number.isInteger(s.count) && s.count>0) &&
    slots.reduce((n,s)=>n+s.count,0)===25 && new Set(slots.map(s=>s.style)).size===2;
  return {total:bank.length,invalid:failures,coverage,
    ready:validBlueprint && bank.length<=10000 && failures.length===0 &&
      coverage.every(c=>c.available>=c.required)};
}
// Explicit cancellations count as abandoned attempts. Reopening a live attempt
// remains a resume, not a cancellation. Never escalate merely for network loss.
export function difficultyAfterCancellations(cancelledAttempts) {
  if (!Number.isSafeInteger(cancelledAttempts) || cancelledAttempts < 0)
    throw new ApiError(503, 'invalid_cancellation_count');
  return cancelledAttempts >= 4 ? 'hard' : cancelledAttempts >= 2 ? 'medium' : 'easy';
}
export function prioritizeDifficulty(slots, cancelledAttempts) {
  const priority=difficultyAfterCancellations(cancelledAttempts);
  const order=priority==='hard'?['hard','medium','easy']:priority==='medium'?['medium','hard','easy']:['easy','medium','hard'];
  return [...slots].sort((a,b)=>order.indexOf(a.difficulty)-order.indexOf(b.difficulty));
}
export function selectQuestions(bank, slots, exposed = new Set(), choose = randomIndex) {
  if (!Array.isArray(slots) || slots.length === 0 || slots.some(s => !s || !Number.isInteger(s.count) || s.count < 1) || slots.reduce((n, s) => n + s.count, 0) !== 25)
    throw new ApiError(503, 'blueprint_requires_25_questions');
  if (new Set(slots.map(s => s.style)).size !== 2) throw new ApiError(503, 'blueprint_requires_two_reviewed_styles');
  if (!Array.isArray(bank) || bank.length > 10000 || bank.some(q => !q || !/^[A-Za-z0-9_-]{1,100}$/.test(q.id)) || new Set(bank.map(q => q.id)).size !== bank.length)
    throw new ApiError(503, 'invalid_question_bank');
  const seen = new Set(exposed), selected = [];
  for (const slot of slots) {
    if (!automaticStyles.has(slot.style) || !Number.isInteger(slot.count) || slot.count < 1 || !slot.objective || !['easy','medium','hard'].includes(slot.difficulty))
      throw new ApiError(503, 'unsupported_blueprint');
    const available = bank.filter(q => !seen.has(q.id) && q.style === slot.style && q.objective === slot.objective && q.difficulty === slot.difficulty);
    if (available.length < slot.count) throw new ApiError(409, 'insufficient_fresh_questions');
    for (let n = 0; n < slot.count; n++) {
      const [question] = available.splice(choose(available.length), 1);
      if (seen.has(question.id)) throw new ApiError(503, 'duplicate_question_id');
      if (!/^[A-Za-z0-9_-]{1,100}$/.test(question.id)) throw new ApiError(503, 'invalid_question_id');
      validateQuestion(question);
      seen.add(question.id); selected.push(question);
    }
  }
  for (let i = selected.length - 1; i > 0; i--) { const j = choose(i + 1); [selected[i], selected[j]] = [selected[j], selected[i]]; }
  return selected;
}
export function assertLive(attempt, now) {
  if (attempt.status !== 'active') throw new ApiError(409, 'attempt_finalized');
  if (now >= attempt.deadline_at) throw new ApiError(410, 'attempt_expired');
}
export function grade(questions, answers, passPercent) {
  if (questions.length !== 25 || !Number.isInteger(passPercent) || passPercent < 1 || passPercent > 100) throw new ApiError(503, 'invalid_assessment');
  const correct = questions.reduce((n, q) => n + (Object.hasOwn(answers, q.question_id) && answers[q.question_id] === q.correct_index ? 1 : 0), 0);
  return { score: correct * 4, passed: correct * 4 >= passPercent };
}
export function publicQuestion(q) { return { id: q.question_id ?? q.id, prompt: q.prompt, choices: JSON.parse(q.choices_json), style: q.style }; }
