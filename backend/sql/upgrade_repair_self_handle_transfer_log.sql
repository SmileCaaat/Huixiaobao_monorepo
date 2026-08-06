-- Create fire_fault_repair_log for work-order timeline.
-- Idempotent. Restart backend after apply.

CREATE TABLE IF NOT EXISTS fire_fault_repair_log (
  log_id         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT 'log id',
  repair_id      BIGINT(20)   NOT NULL COMMENT 'repair id',
  action_type    VARCHAR(32)  NOT NULL COMMENT 'create/dispatch/start/complete/transfer/recall',
  action_content VARCHAR(500) DEFAULT NULL COMMENT 'display text',
  operator_id    BIGINT(20)   DEFAULT NULL COMMENT 'operator user id',
  operator_name  VARCHAR(64)  DEFAULT NULL COMMENT 'operator name',
  create_time    DATETIME     DEFAULT NULL COMMENT 'create time',
  PRIMARY KEY (log_id),
  KEY idx_repair_log_repair (repair_id),
  KEY idx_repair_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='fault repair work order log';
