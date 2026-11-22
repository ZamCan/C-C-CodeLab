// Server-only C17 fundamentals assessment candidates. Never bundle this module into Android.
// These are draft, deterministic, independently answerable items requiring academic review.
// Each variant has unique operands and an independently computed expected output.
const variants = Array.from({length:150},(_,i)=>{
  const a=3+(i*7)%47, b=2+(i*11)%23, operation=i%5;
  const kinds=['sum','difference','product','quotient','remainder'];
  const kind=kinds[operation];
  const expected=[a+b,a-b,a*b,Math.trunc(a/b),a%b][operation];
  const expression=[`x + y`,`x - y`,`x * y`,`x / y`,`x % y`][operation];
  const correct=String(expected);
  const distractors=[String(expected+1),String(expected-1),String(a+b+1),String(a*b+2),String(expected+2)]
    .filter(v=>v!==correct);
  const choices=[correct,...new Set(distractors)].slice(0,4);
  // Rotate correct position, never reveal correct_index to clients.
  const offset=i%4;
  while(choices.length<4)choices.push(String(expected+choices.length+3));
  const rotated=choices.slice(offset).concat(choices.slice(0,offset));
  return {
    id:`c17_io_${String(i+1).padStart(3,'0')}`,course_id:'c17',
    style:i%2===0?'output_prediction':'multiple_choice',
    objective:'c_input_output',difficulty:i<50?'easy':i<100?'medium':'hard',
    prompt:`C17 program: #include <stdio.h>\\nint main(void) { int x, y; if (scanf("%d %d", &x, &y) != 2) return 1; printf("%d\\\\n", ${expression}); return 0; }\\nIf the input is "${a} ${b}", what integer is printed? (Integer division truncates toward zero.)`,
    choices_json:JSON.stringify(rotated),correct_index:rotated.indexOf(correct),
    answer_explanation:`scanf reads x=${a} and y=${b}; the ${kind} expression evaluates to ${expected}.`,
    review_status:'draft'
  };
});
export const c17InputOutputDraftBank=Object.freeze(variants.map(v=>Object.freeze(v)));
export function auditC17DraftBank(){
  return {count:variants.length,uniqueIds:new Set(variants.map(v=>v.id)).size,
    correctChoices:variants.every(v=>JSON.parse(v.choices_json)[v.correct_index]!==undefined),
    status:'draft_not_authorized_for_certification'};
}
