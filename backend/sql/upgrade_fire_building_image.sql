-- 建筑信息增加图片字段（小程序登记/编辑上传用）
-- 可重复执行：列已存在则跳过

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'fire_building'
    AND COLUMN_NAME = 'image'
);

SET @sql := IF(
  @col_exists = 0,
  'ALTER TABLE fire_building ADD COLUMN image varchar(500) DEFAULT NULL COMMENT ''建筑图片'' AFTER refuge_floor',
  'SELECT ''fire_building.image already exists'' AS info'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
