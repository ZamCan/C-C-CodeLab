-- Preserve existing attempts: legacy attempts without a name snapshot cannot issue.
ALTER TABLE attempts ADD COLUMN official_name TEXT;
ALTER TABLE attempts ADD COLUMN name_confirmed_at INTEGER;
UPDATE blueprints SET pass_percent=70;
CREATE TRIGGER blueprint_pass_insert BEFORE INSERT ON blueprints WHEN NEW.pass_percent!=70 BEGIN SELECT RAISE(ABORT,'pass_percent_must_be_70'); END;
CREATE TRIGGER blueprint_pass_update BEFORE UPDATE OF pass_percent ON blueprints WHEN NEW.pass_percent!=70 BEGIN SELECT RAISE(ABORT,'pass_percent_must_be_70'); END;
CREATE TRIGGER attempt_name_immutable BEFORE UPDATE OF official_name ON attempts WHEN OLD.official_name IS NOT NEW.official_name BEGIN SELECT RAISE(ABORT,'exam_name_immutable'); END;
CREATE TRIGGER exam_finalized_audit AFTER UPDATE OF status ON attempts WHEN OLD.status='active' AND NEW.status!='active' BEGIN
 INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at,metadata_json) VALUES(lower(hex(randomblob(16))),NEW.learner_id,'attempt_finalized',NEW.id,NEW.finalized_at,json_object('status',NEW.status,'score',NEW.score,'passed',NEW.passed));
END;
