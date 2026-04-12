-- 修改题目表，添加选择题相关字段
ALTER TABLE question
ADD COLUMN questionType INT DEFAULT 0 NOT NULL COMMENT '题目类型（0 - 编程题，1 - 选择题）',
ADD COLUMN options TEXT NULL COMMENT '选择题选项（json 数组）';

-- 创建索引（可选）
-- ALTER TABLE question ADD INDEX idx_questionType (questionType);
