package controllers;

import models.Student;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.MajorRepository;
import repository.StudentRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class StudentController extends Controller {

    private final FormFactory formFactory;
    private final MajorRepository majorRepository;
    private final StudentRepository studentRepository;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public StudentController(FormFactory formFactory,
                             MajorRepository majorRepository,
                             StudentRepository studentRepository,
                             HttpExecutionContext httpExecutionContext) {
        this.formFactory = formFactory;
        this.majorRepository = majorRepository;
        this.studentRepository = studentRepository;
        this.httpExecutionContext = httpExecutionContext;
    }

    public Result all() {
        List<Student> studentList = Student.find.all();

        return ok(views.html.student.list.render(studentList));
    }

    public Result show(Long id) {
        Student student = Student.find.byId(id);
        return ok(views.html.student.view.render(student));
    }

    public CompletionStage<Result> create() {
        Form<Student> studentForm = this.formFactory.form(Student.class);
        // Run majors db operation and then render the form
        return majorRepository.options().thenApplyAsync((Map<String, String> majors) ->
                ok(views.html.student.create.render(studentForm, majors)), httpExecutionContext.current());
    }

    public CompletionStage<Result> save() {
        Form<Student> studentForm = this.formFactory.form(Student.class).bindFromRequest();
        Student student = studentForm.get();

        return studentRepository.insert(student).thenApplyAsync(studentId ->
                redirect(routes.StudentController.show(studentId)), httpExecutionContext.current()
        );
    }

    public CompletionStage<Result> edit(Long id) {
        CompletionStage<Map<String, String>> majorsOptions = majorRepository.options();

        return studentRepository.lookup(id).thenCombineAsync(majorsOptions, (studentOptional, majors) -> {
            Student student = studentOptional.get();
            Form<Student> studentForm = this.formFactory.form(Student.class).fill(student);
            return ok(views.html.student.edit.render(id, studentForm, majors));
        }, httpExecutionContext.current());
    }

    public CompletionStage<Result> update(Long id) {
        Form<Student> studentForm = this.formFactory.form(Student.class).bindFromRequest();
        Student student = studentForm.get();

        return studentRepository.update(id, student).thenApplyAsync(studentId ->
                redirect(routes.StudentController.show(studentId.get())), httpExecutionContext.current()
        );
    }

    public Result delete(Long id) {
        return ok();
    }
}
