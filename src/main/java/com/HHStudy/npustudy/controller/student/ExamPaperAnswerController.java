package com.HHStudy.npustudy.controller.student;

import com.HHStudy.npustudy.base.BaseApiController;
import com.HHStudy.npustudy.base.RestResponse;
import com.HHStudy.npustudy.domain.*;
import com.HHStudy.npustudy.domain.*;
import com.HHStudy.npustudy.domain.enums.ExamPaperAnswerStatusEnum;
import com.HHStudy.npustudy.event.CalculateExamPaperAnswerCompleteEvent;
import com.HHStudy.npustudy.event.UserEvent;
import com.HHStudy.npustudy.service.ExamPaperAnswerService;
import com.HHStudy.npustudy.service.ExamPaperService;
import com.HHStudy.npustudy.service.SubjectService;
import com.HHStudy.npustudy.utility.DateTimeUtil;
import com.HHStudy.npustudy.utility.ExamUtil;
import com.HHStudy.npustudy.utility.PageInfoHelper;
import com.HHStudy.npustudy.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperReadVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperSubmitVM;
import com.HHStudy.npustudy.viewmodel.student.exampaper.ExamPaperAnswerPageResponseVM;
import com.HHStudy.npustudy.viewmodel.student.exampaper.ExamPaperAnswerPageVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController("StudentExamPaperAnswerController")
@RequestMapping(value = "/api/student/exampaper/answer")
public class ExamPaperAnswerController extends BaseApiController {

    private final ExamPaperAnswerService examPaperAnswerService;
    private final ExamPaperService examPaperService;
    private final SubjectService subjectService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ExamPaperAnswerController(ExamPaperAnswerService examPaperAnswerService, ExamPaperService examPaperService, SubjectService subjectService, ApplicationEventPublisher eventPublisher) {
        this.examPaperAnswerService = examPaperAnswerService;
        this.examPaperService = examPaperService;
        this.subjectService = subjectService;
        this.eventPublisher = eventPublisher;
    }


    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamPaperAnswerPageResponseVM>> pageList(@RequestBody @Valid ExamPaperAnswerPageVM model) {
        model.setCreateUser(getCurrentUser().getId());
        PageInfo<ExamPaperAnswer> pageInfo = examPaperAnswerService.studentPage(model);
        PageInfo<ExamPaperAnswerPageResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamPaperAnswerPageResponseVM vm = modelMapper.map(e, ExamPaperAnswerPageResponseVM.class);
            Subject subject = subjectService.selectById(vm.getSubjectId());
            vm.setDoTime(ExamUtil.secondToVM(e.getDoTime()));
            vm.setSystemScore(ExamUtil.scoreToVM(e.getSystemScore()));
            vm.setUserScore(ExamUtil.scoreToVM(e.getUserScore()));
            vm.setPaperScore(ExamUtil.scoreToVM(e.getPaperScore()));
            vm.setSubjectName(subject.getName());
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }


    @RequestMapping(value = "/answerSubmit", method = RequestMethod.POST)
    public RestResponse answerSubmit(@RequestBody @Valid ExamPaperSubmitVM examPaperSubmitVM) {
        User user = getCurrentUser();
        ExamPaperAnswerInfo examPaperAnswerInfo = examPaperAnswerService.calculateExamPaperAnswer(examPaperSubmitVM, user);
        if (null == examPaperAnswerInfo) {
            return RestResponse.fail(2, "?????????????????????");
        }
        ExamPaperAnswer examPaperAnswer = examPaperAnswerInfo.getExamPaperAnswer();
        Integer userScore = examPaperAnswer.getUserScore();
        String scoreVm = ExamUtil.scoreToVM(userScore);
        UserEventLog userEventLog = new UserEventLog(user.getId(), user.getUserName(), user.getRealName(), new Date());
        String content = user.getUserName() + " ???????????????" + examPaperAnswerInfo.getExamPaper().getName()
                + " ?????????" + scoreVm
                + " ?????????" + ExamUtil.secondToVM(examPaperAnswer.getDoTime());
        userEventLog.setContent(content);
        eventPublisher.publishEvent(new CalculateExamPaperAnswerCompleteEvent(examPaperAnswerInfo));
        eventPublisher.publishEvent(new UserEvent(userEventLog));
        return RestResponse.ok(scoreVm);
    }


    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public RestResponse edit(@RequestBody @Valid ExamPaperSubmitVM examPaperSubmitVM) {
        boolean notJudge = examPaperSubmitVM.getAnswerItems().stream().anyMatch(i -> i.getDoRight() == null && i.getScore() == null);
        if (notJudge) {
            return RestResponse.fail(2, "??????????????????");
        }

        ExamPaperAnswer examPaperAnswer = examPaperAnswerService.selectById(examPaperSubmitVM.getId());
        ExamPaperAnswerStatusEnum examPaperAnswerStatusEnum = ExamPaperAnswerStatusEnum.fromCode(examPaperAnswer.getStatus());
        if (examPaperAnswerStatusEnum == ExamPaperAnswerStatusEnum.Complete) {
            return RestResponse.fail(3, "???????????????");
        }
        String score = examPaperAnswerService.judge(examPaperSubmitVM);
        User user = getCurrentUser();
        UserEventLog userEventLog = new UserEventLog(user.getId(), user.getUserName(), user.getRealName(), new Date());
        String content = user.getUserName() + " ???????????????" + examPaperAnswer.getPaperName() + " ?????????" + score;
        userEventLog.setContent(content);
        eventPublisher.publishEvent(new UserEvent(userEventLog));
        return RestResponse.ok(score);
    }

    @RequestMapping(value = "/read/{id}", method = RequestMethod.POST)
    public RestResponse<ExamPaperReadVM> read(@PathVariable Integer id) {
        ExamPaperAnswer examPaperAnswer = examPaperAnswerService.selectById(id);
        ExamPaperReadVM vm = new ExamPaperReadVM();
        ExamPaperEditRequestVM paper = examPaperService.examPaperToVM(examPaperAnswer.getExamPaperId());
        ExamPaperSubmitVM answer = examPaperAnswerService.examPaperAnswerToVM(examPaperAnswer.getId());
        vm.setPaper(paper);
        vm.setAnswer(answer);
        return RestResponse.ok(vm);
    }


}
