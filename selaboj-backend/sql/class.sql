use selaboj;

-- 班级表
create table if not exists class
(
    id          bigint auto_increment comment 'id' primary key,
    className   varchar(256)                       not null comment '班级名称',
    classCode   varchar(128)                       not null comment '班级代码',
    description text                               null comment '班级描述',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_classCode (classCode)
) comment '班级' collate = utf8mb4_unicode_ci;

-- 班级学生关联表
create table if not exists class_student
(
    id         bigint auto_increment comment 'id' primary key,
    classId    bigint                             not null comment '班级 id',
    userId     bigint                             not null comment '用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_classId (classId),
    index idx_userId (userId)
) comment '班级学生关联' collate = utf8mb4_unicode_ci;
