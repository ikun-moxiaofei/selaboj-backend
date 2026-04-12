use selaboj;

-- 考试表
create table if not exists exam
(
    id          bigint auto_increment comment 'id' primary key,
    examName    varchar(256)                       not null comment '考试名称',
    description text                               null comment '考试描述',
    startTime   datetime                           null comment '开始时间',
    endTime     datetime                           null comment '结束时间',
    totalScore  int      default 100               not null comment '总分',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '考试' collate = utf8mb4_unicode_ci;

-- 考试题目关联表
create table if not exists exam_question
(
    id          bigint auto_increment comment 'id' primary key,
    examId      bigint                             not null comment '考试 id',
    questionId  bigint                             not null comment '题目 id',
    score       int      default 10                not null comment '题目分值',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_examId (examId),
    index idx_questionId (questionId)
) comment '考试题目关联' collate = utf8mb4_unicode_ci;

-- 考试记录表
create table if not exists exam_record
(
    id          bigint auto_increment comment 'id' primary key,
    examId      bigint                             not null comment '考试 id',
    userId      bigint                             not null comment '用户 id',
    totalScore  int      default 0                 not null comment '总分',
    status      int      default 0                 not null comment '状态（0 - 待考试，1 - 考试中，2 - 已完成）',
    startTime   datetime                           null comment '开始时间',
    endTime     datetime                           null comment '结束时间',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_examId (examId),
    index idx_userId (userId)
) comment '考试记录' collate = utf8mb4_unicode_ci;

-- 考试答题记录表
create table if not exists exam_answer
(
    id          bigint auto_increment comment 'id' primary key,
    examRecordId bigint                            not null comment '考试记录 id',
    questionId  bigint                             not null comment '题目 id',
    answer      text                               null comment '答案',
    score       int      default 0                 not null comment '得分',
    status      int      default 0                 not null comment '状态（0 - 未作答，1 - 已作答，2 - 正确，3 - 错误）',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_examRecordId (examRecordId),
    index idx_questionId (questionId)
) comment '考试答题记录' collate = utf8mb4_unicode_ci;
