ALTER TABLE learners ADD COLUMN display_name TEXT NOT NULL DEFAULT '';
ALTER TABLE learners ADD COLUMN locale TEXT NOT NULL DEFAULT 'en' CHECK(locale IN('en','sw'));
ALTER TABLE learners ADD COLUMN role TEXT NOT NULL DEFAULT 'student' CHECK(role IN('student','examiner','admin'));
ALTER TABLE learners ADD COLUMN identity_verified INTEGER NOT NULL DEFAULT 0 CHECK(identity_verified IN(0,1));
ALTER TABLE sessions ADD COLUMN csrf_token TEXT;
ALTER TABLE courses ADD COLUMN title_en TEXT NOT NULL DEFAULT '';
ALTER TABLE courses ADD COLUMN title_sw TEXT NOT NULL DEFAULT '';
CREATE TABLE passkeys(id TEXT PRIMARY KEY, learner_id TEXT NOT NULL REFERENCES learners(id), public_key TEXT NOT NULL, counter INTEGER NOT NULL DEFAULT 0, transports_json TEXT NOT NULL DEFAULT '[]', created_at INTEGER NOT NULL);
CREATE TABLE auth_challenges(id TEXT PRIMARY KEY, challenge TEXT NOT NULL, purpose TEXT NOT NULL CHECK(purpose IN('register','login')), pending_json TEXT NOT NULL, expires_at INTEGER NOT NULL);
CREATE TABLE edge_limits(bucket TEXT NOT NULL, window INTEGER NOT NULL, requests INTEGER NOT NULL, PRIMARY KEY(bucket,window));
CREATE TABLE mobile_codes(code_hash TEXT PRIMARY KEY, learner_id TEXT NOT NULL REFERENCES learners(id), challenge TEXT NOT NULL, expires_at INTEGER NOT NULL);
CREATE INDEX credentials_owner ON credentials(learner_id,issued_at);
CREATE INDEX attempts_owner ON attempts(learner_id,started_at);
CREATE INDEX auth_expiry ON auth_challenges(expires_at);
CREATE INDEX session_expiry ON sessions(expires_at);
CREATE TRIGGER immutable_credential BEFORE UPDATE OF id,attempt_id,learner_id,course_id,course_version,issued_at,key_id,digest,signed_document ON credentials
BEGIN SELECT RAISE(ABORT,'issued_credential_is_immutable'); END;
CREATE TRIGGER prevent_credential_delete BEFORE DELETE ON credentials
BEGIN SELECT RAISE(ABORT,'revoke_instead_of_delete'); END;
CREATE TRIGGER prevent_audit_update BEFORE UPDATE ON audit_events
BEGIN SELECT RAISE(ABORT,'audit_is_append_only'); END;
CREATE TRIGGER prevent_audit_delete BEFORE DELETE ON audit_events
BEGIN SELECT RAISE(ABORT,'audit_is_append_only'); END;
CREATE TRIGGER record_issuance AFTER INSERT ON credentials
BEGIN INSERT INTO audit_events(id,actor_ref,event_type,object_ref,created_at,metadata_json)
VALUES(lower(hex(randomblob(16))),NEW.learner_id,'credential_issued',NEW.id,NEW.issued_at,json_object('digest',NEW.digest,'key_id',NEW.key_id)); END;
CREATE TRIGGER record_revocation AFTER UPDATE OF status ON credentials WHEN OLD.status<>NEW.status
BEGIN INSERT INTO audit_events(id,event_type,object_ref,created_at,metadata_json)
VALUES(lower(hex(randomblob(16))),'credential_status_changed',NEW.id,unixepoch(),json_object('from',OLD.status,'to',NEW.status)); END;
