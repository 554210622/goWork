-- 更新model_tasks表，支持两阶段生成流程
-- 添加生成阶段字段
ALTER TABLE model_tasks ADD COLUMN generation_stage VARCHAR(20) DEFAULT 'single' COMMENT '生成阶段：preview-预览阶段, refine-精细化阶段, single-单阶段';

-- 添加预览任务ID字段
ALTER TABLE model_tasks ADD COLUMN preview_task_id VARCHAR(100) COMMENT '预览任务的Meshy ID（用于精细化阶段）';

-- 更新状态枚举，使用Meshy API的标准状态
ALTER TABLE model_tasks MODIFY COLUMN status ENUM('PENDING', 'IN_PROGRESS', 'SUCCEEDED', 'FAILED') DEFAULT 'PENDING' COMMENT '任务状态：PENDING-等待中, IN_PROGRESS-处理中, SUCCEEDED-已完成, FAILED-失败';

-- 添加索引优化查询性能
CREATE INDEX idx_model_tasks_generation_stage ON model_tasks(generation_stage);
CREATE INDEX idx_model_tasks_preview_task_id ON model_tasks(preview_task_id);

-- 更新现有数据
UPDATE model_tasks SET generation_stage = 'single' WHERE generation_stage IS NULL;
UPDATE model_tasks SET status = 'PENDING' WHERE status = 'pending';
UPDATE model_tasks SET status = 'IN_PROGRESS' WHERE status = 'processing';
UPDATE model_tasks SET status = 'SUCCEEDED' WHERE status = 'completed';
UPDATE model_tasks SET status = 'FAILED' WHERE status = 'failed';