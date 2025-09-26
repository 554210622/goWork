-- 更新model_tasks表，添加task_type字段
ALTER TABLE model_tasks ADD COLUMN task_type VARCHAR(20) DEFAULT 'text' COMMENT '任务类型：text-文本生成, image-图片生成';

-- 更新现有数据的task_type字段
UPDATE model_tasks SET task_type = input_type WHERE task_type IS NULL;

-- 添加索引优化查询性能
CREATE INDEX idx_model_tasks_user_id_status ON model_tasks(user_id, status);
CREATE INDEX idx_model_tasks_task_type ON model_tasks(task_type);
CREATE INDEX idx_model_tasks_created_at ON model_tasks(created_at DESC);