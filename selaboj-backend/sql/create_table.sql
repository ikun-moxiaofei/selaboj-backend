# 数据库初始化

-- 创建库
create database if not exists selaboj;

-- 切换库
use selaboj;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;


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

-- 题目表
create table if not exists question
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text                               null comment '题目答案',
    questionType int      default 0                 not null comment '题目类型（0 - 编程题，1 - 选择题）',
    options     text                               null comment '选择题选项（json 数组）',
    submitNum   int      default 0                 not null comment '题目提交数',
    acceptedNum int      default 0                 not null comment '题目通过数',
    judgeCase   text                               null comment '判题用例（json 数组）',
    judgeConfig text                               null comment '判题配置（json 对象）',
    thumbNum    int      default 0                 not null comment '点赞数',
    favourNum   int      default 0                 not null comment '收藏数',
    userId      bigint                             not null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '题目' collate = utf8mb4_unicode_ci;

-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交';

-- 插入10道简单的编程题
INSERT INTO question (title, content, tags, answer, questionType, judgeCase, judgeConfig, userId) VALUES
-- 1. 两数之和
('两数之和', '输入两个整数a和b，输出它们的和。', '["Java","算法","简单"]', '3', 0,
 '[{"input":"1 2","output":"3"},{"input":"5 7","output":"12"},{"input":"100 200","output":"300"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 2. 两数之差
('两数之差', '输入两个整数a和b，输出a减b的结果。', '["Java","算法","简单"]', '3', 0,
 '[{"input":"5 2","output":"3"},{"input":"10 3","output":"7"},{"input":"100 50","output":"50"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 3. 两数之积
('两数之积', '输入两个整数a和b，输出它们的乘积。', '["Java","算法","简单"]', '6', 0,
 '[{"input":"2 3","output":"6"},{"input":"5 4","output":"20"},{"input":"10 10","output":"100"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 4. 两数之商
('两数之商', '输入两个整数a和b（b不为0），输出a除以b的整数结果。', '["Java","算法","简单"]', '2', 0,
 '[{"input":"10 5","output":"2"},{"input":"15 3","output":"5"},{"input":"100 10","output":"10"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 5. 求余数
('求余数', '输入两个整数a和b（b不为0），输出a除以b的余数。', '["Java","算法","简单"]', '1', 0,
 '[{"input":"10 3","output":"1"},{"input":"15 4","output":"3"},{"input":"20 7","output":"6"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 6. 判断奇偶
('判断奇偶', '输入一个整数n，如果n是偶数输出"even"，否则输出"odd"。', '["Java","算法","简单"]', 'even', 0,
 '[{"input":"4","output":"even"},{"input":"7","output":"odd"},{"input":"10","output":"even"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 7. 求绝对值
('求绝对值', '输入一个整数n，输出它的绝对值。', '["Java","算法","简单"]', '5', 0,
 '[{"input":"5","output":"5"},{"input":"-3","output":"3"},{"input":"0","output":"0"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 8. 求最大值
('求最大值', '输入三个整数a、b、c，输出它们中的最大值。', '["Java","算法","简单"]', '5', 0,
 '[{"input":"1 3 5","output":"5"},{"input":"7 2 4","output":"7"},{"input":"3 9 6","output":"9"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 9. 求最小值
('求最小值', '输入三个整数a、b、c，输出它们中的最小值。', '["Java","算法","简单"]', '1', 0,
 '[{"input":"1 3 5","output":"1"},{"input":"7 2 4","output":"2"},{"input":"3 9 6","output":"3"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1),

-- 10. 交换两数
('交换两数', '输入两个整数a和b，输出交换后的结果（先输出b，再输出a，用空格分隔）。', '["Java","算法","简单"]', '2 1', 0,
 '[{"input":"1 2","output":"2 1"},{"input":"5 3","output":"3 5"},{"input":"10 20","output":"20 10"}]',
 '{"timeLimit":1000,"memoryLimit":102400,"stackLimit":102400}', 1);

-- 插入10道简单的选择题
INSERT INTO question (title, content, tags, answer, questionType, options, userId) VALUES
-- 1. Java基础
('Java基本数据类型', '以下哪个是Java的基本数据类型？', '["Java","基础","选择题"]', 'A', 1,
 '[{"key":"A","value":"int"},{"key":"B","value":"String"},{"key":"C","value":"ArrayList"},{"key":"D","value":"HashMap"}]', 1),

-- 2. Java关键字
('Java关键字', '以下哪个不是Java的关键字？', '["Java","基础","选择题"]', 'D', 1,
 '[{"key":"A","value":"class"},{"key":"B","value":"interface"},{"key":"C","value":"extends"},{"key":"D","value":"main"}]', 1),

-- 3. Java访问修饰符
('Java访问修饰符', '以下哪个访问修饰符的访问范围最大？', '["Java","基础","选择题"]', 'A', 1,
 '[{"key":"A","value":"public"},{"key":"B","value":"protected"},{"key":"C","value":"default"},{"key":"D","value":"private"}]', 1),

-- 4. Java循环
('Java循环语句', '以下哪个是Java的循环语句？', '["Java","基础","选择题"]', 'C', 1,
 '[{"key":"A","value":"if"},{"key":"B","value":"switch"},{"key":"C","value":"for"},{"key":"D","value":"try"}]', 1),

-- 5. Java数组
('Java数组', '以下哪个是正确的数组声明方式？', '["Java","基础","选择题"]', 'B', 1,
 '[{"key":"A","value":"int arr[5];"},{"key":"B","value":"int[] arr = new int[5];"},{"key":"C","value":"array int[5];"},{"key":"D","value":"int[5] arr;"}]', 1),

-- 6. Java字符串
('Java字符串比较', '在Java中，比较两个字符串内容是否相同应该使用哪个方法？', '["Java","基础","选择题"]', 'C', 1,
 '[{"key":"A","value":"=="},{"key":"B","value":"="},{"key":"C","value":"equals()"},{"key":"D","value":"compare()"}]', 1),

-- 7. Java继承
('Java继承', 'Java中一个类可以继承几个父类？', '["Java","面向对象","选择题"]', 'A', 1,
 '[{"key":"A","value":"1个"},{"key":"B","value":"2个"},{"key":"C","value":"3个"},{"key":"D","value":"不限制"}]', 1),

-- 8. Java接口
('Java接口', '以下关于Java接口的说法，哪个是正确的？', '["Java","面向对象","选择题"]', 'B', 1,
 '[{"key":"A","value":"接口中可以有构造方法"},{"key":"B","value":"一个类可以实现多个接口"},{"key":"C","value":"接口不能继承接口"},{"key":"D","value":"接口中的方法必须有实现"}]', 1),

-- 9. Java异常
('Java异常处理', '以下哪个关键字用于捕获异常？', '["Java","异常处理","选择题"]', 'A', 1,
 '[{"key":"A","value":"catch"},{"key":"B","value":"throw"},{"key":"C","value":"throws"},{"key":"D","value":"finally"}]', 1),

-- 10. Java集合
('Java集合框架', '以下哪个集合类是线程安全的？', '["Java","集合","选择题"]', 'D', 1,
 '[{"key":"A","value":"ArrayList"},{"key":"B","value":"HashMap"},{"key":"C","value":"LinkedList"},{"key":"D","value":"Vector"}]', 1);


select * from user