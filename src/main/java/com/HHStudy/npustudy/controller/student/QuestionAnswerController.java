package com.HHStudy.npustudy.controller.student;

import com.HHStudy.npustudy.base.BaseApiController;
import com.HHStudy.npustudy.base.RestResponse;
import com.HHStudy.npustudy.domain.ExamPaperQuestionCustomerAnswer;
import com.HHStudy.npustudy.domain.Subject;
import com.HHStudy.npustudy.domain.TextContent;
import com.HHStudy.npustudy.domain.question.QuestionObject;
import com.HHStudy.npustudy.service.ExamPaperQuestionCustomerAnswerService;
import com.HHStudy.npustudy.service.QuestionService;
import com.HHStudy.npustudy.service.SubjectService;
import com.HHStudy.npustudy.service.TextContentService;
import com.HHStudy.npustudy.utility.DateTimeUtil;
import com.HHStudy.npustudy.utility.HtmlUtil;
import com.HHStudy.npustudy.utility.JsonUtil;
import com.HHStudy.npustudy.utility.PageInfoHelper;
import com.HHStudy.npustudy.viewmodel.admin.question.QuestionEditRequestVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperSubmitItemVM;
import com.HHStudy.npustudy.viewmodel.student.question.answer.QuestionAnswerVM;
import com.HHStudy.npustudy.viewmodel.student.question.answer.QuestionPageStudentRequestVM;
import com.HHStudy.npustudy.viewmodel.student.question.answer.QuestionPageStudentResponseVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("StudentQuestionAnswerController")
@RequestMapping(value = "/api/student/question/answer")
public class QuestionAnswerController extends BaseApiController {

    private final ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService;
    private final QuestionService questionService;
    private final TextContentService textContentService;
    private final SubjectService subjectService;

    @Autowired
    public QuestionAnswerController(ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService, QuestionService questionService, TextContentService textContentService, SubjectService subjectService) {
        this.examPaperQuestionCustomerAnswerService = examPaperQuestionCustomerAnswerService;
        this.questionService = questionService;
        this.textContentService = textContentService;
        this.subjectService = subjectService;
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public RestResponse<PageInfo<QuestionPageStudentResponseVM>> pageList(@RequestBody QuestionPageStudentRequestVM model) {
        model.setCreateUser(getCurrentUser().getId());
        PageInfo<ExamPaperQuestionCustomerAnswer> pageInfo = examPaperQuestionCustomerAnswerService.studentPage(model);
        PageInfo<QuestionPageStudentResponseVM> page = PageInfoHelper.copyMap(pageInfo, q -> {
            Subject subject = subjectService.selectById(q.getSubjectId());
            QuestionPageStudentResponseVM vm = modelMapper.map(q, QuestionPageStudentResponseVM.class);
            vm.setCreateTime(DateTimeUtil.dateFormat(q.getCreateTime()));
            TextContent textContent = textContentService.selectById(q.getQuestionTextContentId());
            QuestionObject questionObject = JsonUtil.toJsonObject(textContent.getContent(), QuestionObject.class);
            String clearHtml = HtmlUtil.clear(questionObject.getTitleContent());
            vm.setShortTitle(clearHtml);
            vm.setSubjectName(subject.getName());
            return vm;
        });
        return RestResponse.ok(page);
    }


    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<QuestionAnswerVM> select(@PathVariable Integer id) {
        QuestionAnswerVM vm = new QuestionAnswerVM();
        ExamPaperQuestionCustomerAnswer examPaperQuestionCustomerAnswer = examPaperQuestionCustomerAnswerService.selectById(id);
        ExamPaperSubmitItemVM questionAnswerVM = examPaperQuestionCustomerAnswerService.examPaperQuestionCustomerAnswerToVM(examPaperQuestionCustomerAnswer);
        QuestionEditRequestVM questionVM = questionService.getQuestionEditRequestVM(examPaperQuestionCustomerAnswer.getQuestionId());
        vm.setQuestionVM(questionVM);
        vm.setQuestionAnswerVM(questionAnswerVM);
        return RestResponse.ok(vm);
    }

}
