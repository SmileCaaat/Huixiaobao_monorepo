-- Optional cleanup only. Do NOT run automatically.
-- Clears period year/num from existing tasks after UI fields were removed.
UPDATE fire_maintenance_task
SET period_year = NULL,
    period_num = NULL;
