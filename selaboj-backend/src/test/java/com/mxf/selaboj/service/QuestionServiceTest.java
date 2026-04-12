package com.mxf.selaboj.service;

import com.mxf.selaboj.model.dto.question.QuestionAddRequest;
import com.mxf.selaboj.model.dto.question.QuestionOption;
import com.mxf.selaboj.model.entity.Question;
import com.mxf.selaboj.model.entity.User;
import com.mxf.selaboj.service.impl.QuestionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class QuestionServiceTest {

    @Resource
    private QuestionService questionService;

    @Test
    void testCreateChoiceQuestion() {
        // 创建选择题测试
        QuestionAddRequest request = new QuestionAddRequest();
        request.setTitle("测试选择题");
        request.setContent("以下哪个是Java的关键字？");
        
        // 设置标签
        List<String> tags = new ArrayList<>();
        tags.add("Java");
        tags.add("选择题");
        request.setTags(tags);
        
        // 设置答案
        request.setAnswer("A");
        
        // 设置题目类型为选择题
        request.setQuestionType(1);
        
        // 设置选项
        List<QuestionOption> options = new ArrayList<>();
        QuestionOption optionA = new QuestionOption();
        optionA.setKey("A");
        optionA.setValue("class");
        options.add(optionA);
        
        QuestionOption optionB = new QuestionOption();
        optionB.setKey("B");
        optionB.setValue("hello");
        options.add(optionB);
        
        QuestionOption optionC = new QuestionOption();
        optionC.setKey("C");
        optionC.setValue("world");
        options.add(optionC);
        
        QuestionOption optionD = new QuestionOption();
        optionD.setKey("D");
        optionD.setValue("test");
        options.add(optionD);
        
        request.setOptions(options);
        
        // 模拟用户
        User user = new User();
        user.setId(1L);
        
        // 测试创建题目
        try {
            Question question = new Question();
            org.springframework.beans.BeanUtils.copyProperties(request, question);
            question.setUserId(1L);
            question.setFavourNum(0);
            question.setThumbNum(0);
            question.setSubmitNum(0);
            question.setAcceptedNum(0);
            
            // 校验题目
            questionService.validQuestion(question, true);
            
            // 保存题目
            boolean saved = questionService.save(question);
            Assertions.assertTrue(saved, "选择题创建失败");
            System.out.println("选择题创建成功，ID: " + question.getId());
            
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    void testCreateProgrammingQuestion() {
        // 创建编程题测试
        QuestionAddRequest request = new QuestionAddRequest();
        request.setTitle("测试编程题");
        request.setContent("编写一个Hello World程序");
        
        // 设置标签
        List<String> tags = new ArrayList<>();
        tags.add("Java");
        tags.add("编程题");
        request.setTags(tags);
        
        // 设置答案
        request.setAnswer("public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello World\"); } }");
        
        // 设置题目类型为编程题
        request.setQuestionType(0);
        
        // 测试创建题目
        try {
            Question question = new Question();
            org.springframework.beans.BeanUtils.copyProperties(request, question);
            question.setUserId(1L);
            question.setFavourNum(0);
            question.setThumbNum(0);
            question.setSubmitNum(0);
            question.setAcceptedNum(0);
            question.setJudgeCase("[{\"input\": \"\", \"output\": \"Hello World\"}]");
            question.setJudgeConfig("{\"timeLimit\": 1000, \"memoryLimit\": 102400, \"stackLimit\": 102400}");
            
            // 校验题目
            questionService.validQuestion(question, true);
            
            // 保存题目
            boolean saved = questionService.save(question);
            Assertions.assertTrue(saved, "编程题创建失败");
            System.out.println("编程题创建成功，ID: " + question.getId());
            
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("测试失败: " + e.getMessage());
        }
    }
}
