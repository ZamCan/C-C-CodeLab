from pathlib import Path
import sqlite3,unittest
ROOT=Path(__file__).resolve().parents[1]

class DatabaseTests(unittest.TestCase):
    def setUp(self):
        self.db=sqlite3.connect(':memory:')
        self.db.executescript((ROOT/'backend/migrations/0001_initial.sql').read_text())
        self.db.execute("INSERT INTO learners VALUES('learner','trusted-account',1)")
        self.db.execute("INSERT INTO courses VALUES('c','1','criteria')")
        self.db.execute("INSERT INTO blueprints VALUES('bp','c','1','[]',1800,80,60,1)")
    def tearDown(self):self.db.close()
    def attempt(self,id='a'):
        self.db.execute("INSERT INTO attempts(id,learner_id,course_id,blueprint_id,policy_version,started_at,deadline_at,status,pass_percent,cooldown_seconds) VALUES(?,'learner','c','bp','1',1,100,'active',80,60)",(id,))
    def test_only_one_active_attempt(self):
        self.attempt()
        with self.assertRaises(sqlite3.IntegrityError):self.attempt('b')
    def test_compare_and_swap_does_not_grade_stale_answers(self):
        self.attempt()
        self.db.execute('UPDATE attempts SET answers_json=? WHERE id=?',('{"q":1}','a'))
        changed=self.db.execute("UPDATE attempts SET status='submitted',score=100 WHERE id='a' AND status='active' AND answers_json='{}'").rowcount
        self.assertEqual(changed,0)
    def test_expiry_prevents_answer_write(self):
        self.attempt()
        self.assertEqual(self.db.execute("UPDATE attempts SET answers_json=json_set(answers_json,'$.q',0) WHERE id='a' AND status='active' AND deadline_at>100").rowcount,0)
    def test_finalized_attempt_cannot_be_changed_by_save_query(self):
        self.attempt()
        self.db.execute("UPDATE attempts SET status='submitted' WHERE id='a'")
        self.assertEqual(self.db.execute("UPDATE attempts SET answers_json='{}' WHERE id='a' AND status='active'").rowcount,0)
    def test_quota_increments_atomically_then_stops(self):
        sql='INSERT INTO rate_limits VALUES(?,?,1) ON CONFLICT(session_hash,window) DO UPDATE SET requests=requests+1 WHERE requests<60 RETURNING requests'
        for count in range(1,61):self.assertEqual(self.db.execute(sql,('hash',1)).fetchone()[0],count)
        self.assertIsNone(self.db.execute(sql,('hash',1)).fetchone())
    def test_database_roundtrip_preserves_state(self):
        self.attempt()
        backup=sqlite3.connect(':memory:');self.db.commit();self.db.backup(backup)
        self.assertEqual(backup.execute('SELECT id,deadline_at FROM attempts').fetchone(),('a',100));backup.close()

unittest.main()
