/**
 * Created by nsmenon on 6/1/2017.
 */
//Report1 Varriables
var perProblemSetReport;
var perProblemSetLevelOne;
var perProblemSetLevelTwo;
var perProblemSetColumnNamesMap;
var perProblemSetLevelOneAvg;
var perProblemSetLevelOneMax;
var perProblemSetLevelOneLatest;

//Report2 Varriables
var perProblemReportTable

//Report3 Varriables
var perClusterReportTable

//Report5 Varribales
var perStudentReport;
var effortMap;
var eachStudentData = [];
var activetable;
var inactivetable;
var studentRosterTable;

var effortLabelMap = {"SKIP" : "The student SKIPPED the problem (didn't do anything on the problem)",
                       "NOTR" : "NOT even READING the problem --The student answered too fast, in less than 4 seconds",
                        "GIVEUP" : "The student started working on the problem, but then GAVE UP and moved on without solving it correctly.",
                        "SOF" :  "The student SOLVED the problem correctly on the FIRST attempt, without any help.",
                        "ATT" : "The student ATTEMPTED once incorrectly, but self-corrected (answered correctly) in the second attempt, no help.",
                        "GUESS" : "The student apparently GUESSED, clicked through 3-5 answers until getting the right one.",
                        "SHINT" : "Student SOLVED problem correctly after seeing HINTS.",
                        "SHELP" : "Got the problem correct but saw atleast one video.",
                        "NO DATA" : "No data could be gathered."
                        }

function loadEffortMap (rows) {
    var effortChartIdSelector = "#effortChart"+rows;
    var containerChartSelector = "#containerChart"+rows;
    var legendChart = "#legendChart"+rows;
    var effortValues = effortMap[rows];
    $("#iconID"+rows).hide();
    var data = {
        labels: ["Type of behaviour in problem"],
        datasets: [{
            label: 'SKIP: The student SKIPPED the problem (did not do anything on the problem)',
            backgroundColor: "#8dd3c7",
            data: [effortValues[0]],
        },
            {
                label: 'NOTR: NOT even READING the problem --The student answered too fast, in less than 4 seconds',
                backgroundColor: "#ffffb3",
                data: [effortValues[1]],
            },
            {
                label: 'GIVEUP: The student started working on the problem, but then GAVE UP and moved on without solving it correctly.',
                backgroundColor: "#bebada",
                data: [effortValues[2]],
            },
            {
                label: 'SOF: The student SOLVED the problem correctly on the FIRST attempt, without any help.',
                backgroundColor: "#fb8072",
                data: [effortValues[3]],
            },
            {
                label: 'ATT: The student ATTEMPTED once incorrectly, but self-corrected (answered correctly) in the second attempt, no help.',
                backgroundColor: "#b3de69",
                data: [effortValues[4]],
            },
            {
                label: 'GUESS: The student apparently GUESSED, clicked through 3-5 answers until getting the right one',
                backgroundColor: "#fccde5",
                data: [effortValues[5]],
            },
            {
                label: 'SHINT: Student SOLVED problem correctly after seeing HINTS.',
                backgroundColor: "#80b1d3",
                data: [effortValues[6]],
            },
            {
                label: 'SHELP: Got the problem correct but saw atleast one video.',
                backgroundColor: "#fdb462",
                data: [effortValues[7]],
            },
            {
                label: 'NO DATA: No data could be gathered.',
                backgroundColor: "#d9d9d9",
                data: [effortValues[8]],
            },
        ]
    };
    var myBarChart = new Chart( $(effortChartIdSelector), {
        type: 'bar',
        data: data,
        options: {
            legend: {
                display: false,
                position: 'bottom',
            },
            legendCallback: function(chart) {
                var text = [];
                text.push('<table align="center" class="' + chart.id + '-legend">');
                for (var i = 0; i < chart.data.datasets.length; i++) {
                    text.push('<tr><td style="padding-right: 10px;" ><span style="background-color:' + chart.data.datasets[i].backgroundColor + '">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></td>');
                    if (chart.data.datasets[i].label) {
                        text.push('<td>'+ chart.data.datasets[i].label + '</td></tr>');
                    }
                }
                text.push('</table>');
                return text.join('');
            },scales: {
            yAxes: [{
                display: true,
                ticks: {
                    suggestedMin: 0,
                    max: 100,
                    callback: function(value, index, values) {
                        return value + '%';
                    }
                },
                scaleLabel: {
                    display: true,
                    labelString: '% times behavior occurred'
                }
            }]
        }
        }
    });
    $(legendChart).prepend(myBarChart.generateLegend());
    $(containerChartSelector).show();
}

function resetStudentData( title,studentId) {
    $.ajax({
        type : "POST",
        url :pgContext+"/tt/tt/resetStudentdata",
        data : {
            studentId: studentId,
            action: title
        },
        success : function(response) {
            if (response.includes("***")) {
                $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                $('#errorMsgModelPopup').modal('show');
            }else{
                $("#successMsgModelPopup").find("[class*='modal-body']").html("Student Info updated. Please refresh page to reflect changes");
                $('#successMsgModelPopup').modal('show');
            }
        }
    });
    return false;

}

function resetPassWordForThisStudent(id,uname){
    var newPassWordToSet = $("#resetPasswordfor"+id).serializeArray()[0].value;
    console.log(newPassWordToSet);
     $.ajax({
         type : "POST",
         url :pgContext+"/tt/tt/resetStudentPassword",
         data : {
             studentId: id,
             userName: uname,
             newPassWord : newPassWordToSet
         },
         success : function(response) {
             if (response.includes("***")) {
                 $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                 $('#errorMsgModelPopup').modal('show');
             }else{
                 $("#successMsgModelPopup").find("[class*='modal-body']").html( "The Password for the student is reset. The new password is"+response+"");
                 $('#successMsgModelPopup').modal('show');
             }
         }
     });
    return false;

}


function updateStudentInfo(formName){
    var dataForm = $("#edit_Student_Form"+formName).serializeArray();
    var values = [];
    $.each(dataForm, function(i, field){
        values[i] = field.value;
    });
    $.ajax({
        type : "POST",
        url :pgContext+"/tt/tt/editStudentInfo",
        data : {
            studentId: formName,
            formData: values
        },
        success : function(response) {
            if (response.includes("***")) {
                $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                $('#errorMsgModelPopup').modal('show');
            }else{
                $("#successMsgModelPopup").find("[class*='modal-body']").html( response );
                $('#successMsgModelPopup').modal('show');
            }
        }

    });
}

function editStudentInformation(id,fname,lname,uname,context){
    var tr = context.closest('tr')
    var row = $('#student_roster').DataTable().row( tr );

    if ( row.child.isShown() ) {
        row.child( false ).remove();
    }else{
       // var editStudentInfoDiv = $($('#editStudentInfoDiv').html());
        if(fname == ''){
            var tempStudentName =  '<div class="form-group"><div class="input-group"><label for="studentFname">First Name</label></div><div class="input-group">'+
                '<input type="text" id="studentFname" class="form-control" name="studentFname" /></div></div>';
        }else{
            var tempStudentName =  '<div class="form-group"><div class="input-group"><label for="studentFname">First Name</label></div><div class="input-group">'+
                '<input type="text" value='+fname+' id="studentFname" class="form-control" name="studentFname" /></div></div>';
        }

        if(lname == ''){
            var tempStudentLastName =  '<div class="form-group"><div class="input-group"><label for="studentLname">Last Name</label></div><div class="input-group">'+
                '<input type="text" id="studentLname" class="form-control" name="studentLname" /></div></div>';

        }   else{
            var tempStudentLastName =  '<div class="form-group"><div class="input-group"><label for="studentLname">Last Name</label></div><div class="input-group">'+
                '<input type="text" value='+lname+' id="studentLname" class="form-control" name="studentLname" /></div></div>';
        }

        var tempStudentUserName =  '<div class="form-group"><div class="input-group"><label for="studentUsername">Username</label></div><div class="input-group">'+
            '<input type="text" value='+uname+' id="studentUsername" class="form-control" name="studentUsername"/></div></div>';

        var formHtml = '<div class="panel-body"><form id="edit_Student_Form'+id+'" onsubmit="event.preventDefault();"><div class="form-group"><div class="input-group"><label for="studentId">UserId</label></div><div class="input-group">'+
            '<input type="text" value='+id+' id="studentId" class="form-control" name="studentId" disabled="disabled" /></div></div>'+tempStudentUserName
            + tempStudentName + tempStudentLastName +
            '<div class="input-group"><button role="button" onclick="updateStudentInfo('+id+')" class="btn btn-primary">Update Information</button></div></form></div>';

        var formHtmlPassWord = '<div class="panel-body"><form id="resetPasswordfor'+id+'" onsubmit="event.preventDefault();"><div class="form-group"><div class="input-group"><label for="newPassword">New Password</label></div><div class="input-group">'+
            '<input type="password"  placeholder="New password to be set" id="newPassword" class="form-control" name="newPassword"/></div></div>' +
            '<div class="input-group"><button role="button" onclick="resetPassWordForThisStudent('+id+',\'' + uname + '\')" type="button" class="btn btn-primary">Reset Password</button></div></form></div>';


        console.log(formHtml);
        var tabPanel = '<div style="width: 40%"> <ul class="nav nav-tabs" role="tablist"> <li class="active"> ' +
            '<a href="#home'+id+'" role="tab" data-toggle="tab"> <i class="fa fa-address-card-o" aria-hidden="true"></i> Update Student Information </a> </li> ' +
            '<li><a href="#profile'+id+'" role="tab" data-toggle="tab"> <i class="fa fa-key" aria-hidden="true"></i> Reset Password for Student </a> </li> </ul>'+
            '<div class="tab-content"> <div class="tab-pane fade active in" id="home'+id+'">'+formHtml+'</div><div class="tab-pane fade" id="profile'+id+'">'+formHtmlPassWord+'</div> </div> </div>';

        row.child(tabPanel).show();

    }

}

function problemDetails(data, response) {
    var JSONData = JSON.parse(response);
    var standards = JSONData["topicStandars"];
    var problems = JSONData["problems"];
    var html = "";
    $.each(standards, function (i, obj) {
        html += '<span style="margin-right: 10px;"><a href=' + obj.url + '>' + obj.code + '</a></span>';
    });
    var selector = "#"+JSONData["problemLevelId"]+"_handler";
    $(document.body).on('click', selector ,function(){
        var rows = $("#"+JSONData["problemLevelId"]).dataTable({ "bPaginate": false,  "bFilter": false,  "bLengthChange": false, rowReorder: false, "bSort": false}).fnGetNodes();
        var rowsArray = [];
        var problemIds = [""];
        var i = 0;
        $("input:checkbox:not(:checked)", rows).each(function(){
            rowsArray[i] = $(this).closest('tr');
            i++;
        });
        for(var j=0; j < rowsArray.length; j++)
            problemIds      [j]  = $("#"+JSONData["problemLevelId"]).DataTable().row( rowsArray [j] ).data()[1];

        $.ajax({
            type : "POST",
            url :pgContext+"/tt/tt/saveChangesForProblemSet",
            data : {
                problemIds: problemIds,
                classid: classID,
                problemsetId: JSONData["problemLevelId"]
            },
            success : function(response) {
                if (response.includes("***")) {
                    $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                    $('#errorMsgModelPopup').modal('show');
                }else{
                    $("#successMsgModelPopupForProblemSets").find("[class*='modal-body']").html( "Content changes saved successfully." );
                    $('#successMsgModelPopupForProblemSets').modal('show');
                }
            }
        });

    });

    var higherlevelDetail = "<div id=" + data[0] + " class='panel-body animated zoomOut'> " +
        " <div class='panel panel-default'> <div class='panel-body'><strong>Problem Set: " + JSONData["topicName"] + "</strong></div> " +
        " <div class='panel-body'><strong>Standards Covered in this Problem Set: " + html + "</strong></div>" +
        " <div class='panel-body'><strong>Summary : " + JSONData["topicSummary"] + "</strong></div>"+
        "<div class='panel-body'>Students will see the following selected problems for this problem set,within the grade range that you selected. Feel free to Check/Uncheck  problems you wish to add/remove and remember to Click on the 'SAVE' button below </div>"+
        "<div class='panel-body'> <button id="+JSONData["problemLevelId"]+'_handler'+" class='btn btn-primary btn-lg' aria-disabled='true'>Save Changes</button></div></div>";


    return higherlevelDetail + problemLevelDetails(JSONData,problems);

}

function problemLevelDetails(JSONData,problems){
    var tableHeader = '<table id='+JSONData["problemLevelId"]+' class="table table-striped table-bordered hover" cellspacing="0" width="100%"><thead><tr><th>Activated</th><th>ID</th><th>Name</th><th>Nickname</th><th>Difficulty</th><th>CC Standard</th><th>Type</th></tr></thead><tbody>';
    var attri = ", 'ProblemPreview'"+","+"'width=750,height=550,status=yes,resizable=yes'";
    $.each(problems, function (i, obj) {
        var html = "";
        var flash = "";
        var checkBox = "";
        var flashWindow = "'" + JSONData["uri"]+"?questionNum="+obj.problemNo + "'" + attri ;
        var htmlWindow =  "'" + JSONData["html5ProblemURI"]+obj.htmlDirectory+"/"+obj.resource+ "'" + attri;
        $.each(obj.ccStand, function (i, obj) {
            html += '<span style="margin-right: 10px;"><a href=' + obj.url + '>' + obj.code + '</a></span>';
        });
        if(obj.type=='flash'){
            flash = '<td><a onclick="window.open('+flashWindow+');">'+obj.name+'</a></td>';
        }else{
            flash = '<td><a onclick="window.open('+htmlWindow+');">'+obj.name+'</a></td>';
        }
        if(obj.activated){
            checkBox =  "<tr><td><input type='checkbox' name='activated' checked='checked'></td>"
        }else{
            checkBox =  "<tr><td><input type='checkbox' name='activated'></td>"
        }
        tableHeader +=  checkBox+
            "<td>"+obj.id+"</td>"+
            flash+
            "<td>"+obj.nickName+"</td>"+
            "<td>"+obj.difficulty+"</td>"+
            "<td>"+html+"</td>"+
            "<td>"+obj.type+"</td></tr>";
    });
    return tableHeader + "</tbody><table></div>";
}

function handleclickHandlers() {
    $('#reports_handler').click(function () {
        $('#reorg_prob_sets_handler').css('background-color', '');
        $('#reorg_prob_sets_handler').css('color', '#dddddd');

        $("#content-conatiner").children().hide();
        $("#report-wrapper").show();
    });

    $("#copyClass_handler").click(function () {
        $('#reorg_prob_sets_handler').css('background-color', '');
        $('#reorg_prob_sets_handler').css('color', '#dddddd');

        $("#content-conatiner").children().hide();
        $("#clone_class_out").show();
    });

    $("#reorg_prob_sets_handler").click(function () {
        $('#reorg_prob_sets_handler').css('color', '#ffffff');

        $("#content-conatiner").children().hide();
        $("#problem_set_content").show();
    });

    $("#resetSurveySettings_handler").click(function () {
        $('#reorg_prob_sets_handler').css('background-color', '');
        $('#reorg_prob_sets_handler').css('color', '#dddddd');

        $("#content-conatiner").children().hide();
        $("#reset_survey_setting_out").show();
    });


    $("#addMoreStudentsToClass").click(function () {
        $("#addMoreStudents").show();
        $("#addMoreStudentsToClass").prop('disabled', true);
    });

    $("#cancelForm").click(function () {
        $("#addMoreStudents").hide();
        $("#addMoreStudentsToClass").prop('disabled', false);
    });

    $("#reconfigure_student_handler").click(function () {
        $('#reorg_prob_sets_handler').css('background-color', '');
        $('#reorg_prob_sets_handler').css('color', '#dddddd');

        $("#content-conatiner").children().hide();
        $("#student_roster_out").show();
    });

    $('#activateProbSetTable input[type="checkbox"]').click(function () {
        if ($('#activateProbSetTable input[type="checkbox"]:checked').size()) {
            $('#deacivateProblemSets').prop('disabled', false);
        } else {
            $('#deacivateProblemSets').prop('disabled', true);
        }
    });

    $('#inActiveProbSetTable input[type="checkbox"]').click(function () {
        if ($('#inActiveProbSetTable input[type="checkbox"]:checked').size()) {
            $('#acivateProblemSets').prop('disabled', false);
        } else {
            $('#acivateProblemSets').prop('disabled', true);
        }
    });

    $('a[rel=initialPopover]').popover({
        html: true,
        trigger: 'hover',
        container: 'body',
        title: 'What is Mastery ?',
        placement: 'right',
        content: function () {
            return "<ul><li>Student 'Mastery' is MathSpring's estimation of how much the student has demonstrated to know the topic, ranging from zero (0) to one (1)</li>" +
                "<li>Example: IF a student solves 4 problems correctly in a row without mistakes and no help requests, they get a mastery of 0.85 and mastered the topic.</li>" +
                "(Note: also, these 4 problems would be increasingly harder, because as students continue to solve these problems correctly, problems get harder).  " +
                "Mastery would increase slower if the student asks for hints. Mastery would decrease if students make mistakes";
        }
    });

    $('a[rel="popoverOrder"]').popover({
        html: false,
        trigger: 'hover',
        container: 'body',
        placement: 'top',
        content: function () {
            return 'Order in which the ProblemSet will be shown to the student.';
        }
    });
    $('a[rel="popoveractivatedProblems"]').popover({
        html: false,
        trigger: 'hover',
        container: 'body',
        placement: 'top',
        content: function () {
            return 'Number of Activated Problems. Click on the "V" arrow to see all problems and activate more.';
        }
    });


}

function registerAllEvents(){
    $('#wrapper').toggleClass('toggled');
    $('#reorg_prob_sets_handler').css('background-color','#e6296f');
    $('#reorg_prob_sets_handler').css('color', '#ffffff');


   $('#perTopicReportLegendTable').DataTable({
       "bPaginate": false,
       "bFilter": false,
       "bLengthChange": false,
       "ordering": false
   });

    $('#perProblemReportLegendTable').DataTable({
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        "ordering": false
    });

    $('#perClusterLegendTable').DataTable({
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        "ordering": false
    });


    $('#masteryTrajecotoryLegend').DataTable({
        "columnDefs" : [{"title" : "Problem ID", "targets": [0]},{"title" : "Problem Name", "targets": [1]},{"title" : "Student Effort", "targets": [2]}],
        destroy: true,
        "bFilter": false,
        "bLengthChange": false,
        "ordering": false
    });


    perProblemSetReport = $('#perTopicStudentReport').DataTable({
        data: [],
        destroy: true,
        columns: [
            { title: "Student ID" },
            { title: "Student Name" },
            { title: "Username" },
            { title: "No of problems attempted" },
            { title: "Effort Chart" },
        ],
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        rowReorder: false,
        "bSort" : false,

    } );

    perProblemReportTable = $('#perProblemReport').DataTable({
        data: [],
        destroy: true,
        columns: [
            { title: "Problem ID", data : "problemId" },
            { title: "Problem Name", data : "problemName" },
            { title: "# of Students seen the problem", data : "noStudentsSeenProblem" },
            { title: "# of Students solved the problem", data : "getPercStudentsSolvedEventually" },
            { title: "# of Students solved the problem on the first attempt", data : "getGetPercStudentsSolvedFirstTry" },
            { title: "# of Students solved the problem on the second attempt", data : "getGetPercStudentsSolvedSecondTry" },
            { title: "# of Students repeated the problem", data : "percStudentsRepeated" },
            { title: "# of Students skipped the problem", data : "percStudentsSkipped" },
            { title: "# of Students gave up", data : "percStudentsGaveUp" },
            { title: "Most Frequent Incorrect Response", data : "mostIncorrectResponse" }
        ],
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        rowReorder: false

    } );

    perClusterReportTable = $('#perClusterReport').DataTable({
        data: [],
        destroy: true,
        columns: [
            { title: "Cluster Name", data : "clusterName" },
            { title: "# of problems in cluster", data : "noOfProblemsInCluster" },
            { title: "% solved in the first attempt", data : "noOfProblemsonFirstAttempt" },
            { title: "Avg ratio of hint requested", data : "totalHintsViewedPerCluster" }
        ],
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        rowReorder: false

    } );

    perStudentReport =  $('#perStudentReport').DataTable({
        data: [],
        destroy: true,
        columns: [
            { title: "Student ID" },
            { title: "Student Name" },
            { title: "Username" },
            { title: "No of problems attempted" },
            { title: "Effort Chart" },
        ],
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        rowReorder: false,
        "bSort" : false,
        "columnDefs": [
            {
                "width": "10%",
                "targets": [ 0 ],
                "visible": false

            },{
                "width": "10%",
                "targets": [ 1 ],
                "visible": true

            },{
                "width": "10%",
                "targets": [ 2 ],
                "visible": true

            },
            {
                "width": "10%",
                "targets": [ 3 ],
                "visible": true,
                'className': 'dt-body-center',
                'render': function ( data, type, row ) {
                    return '<label>'+data+'&nbsp&nbsp</lable><a  class="viewEachStudentDetail" aria-expanded="true" aria-controls="collapseOne"><i class="glyphicon glyphicon-menu-down"></i></a>';
                }

            },
            {
                "targets": [ 4 ],
                "width": "60%",
                'className': 'dt-body-center',
                'render': function ( data, type, row ) {
                    var effortChartId = "effortChart"+row[0];
                    var containerChart = "containerChart"+row[0];
                    var legendChart = "legendChart"+row[0];
                    var dataContent = "<div id="+containerChart+" style='width:900px;height:680px;display:none'><canvas id="+effortChartId+"></canvas> <div id='"+legendChart+"'></div></div>";
                    return "<i id='iconID"+row[0]+"' style='cursor:pointer;' class='fa fa-th' aria-hidden='true' onclick='loadEffortMap("+row[0]+");'></i>"+dataContent;
                }
            },
        ]
    } );

    activetable = $('#activateProbSetTable').DataTable({
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        rowReorder: true,
        "columnDefs": [
            {
                "targets": [ 0 ],
                "width": "10%",
                'className': 'reorder',
                orderable: false
            },
            {
                "targets": [ 1 ],
                "width": "30%",
                "orderable": false,
            },
            {
                "targets": [ 2 ],
                orderable: false,
                "width": "10%",
            },
            {
                "width": "30%",
                "targets": [ 3 ],
                "visible": false,
                "orderable": false,

            },
            {
                "targets": [ -1 ],
                "orderable": false,
                "width": "20%",
                'className': 'dt-body-center',
                'render': function (data, type, full, meta){
                    return '<input type="checkbox">';
                }
            },
        ]

    });


    inactivetable = $('#inActiveProbSetTable').DataTable({
        "bPaginate": false,
        "bFilter": false,
        "bSort" : false,
        "bLengthChange": false,
        rowReorder: false,
        "bSort" : false,
        "columnDefs": [
            {
                "targets": [ 0 ],
                "width": "10%",
                orderable: true
            },
            {


                "targets": [ 1 ],
                "width": "30%"
            },
            {
                "targets": [ 2 ],
                orderable: false,
                "width": "10%",
            },
            {
                "width": "30%",
                "targets": [ 3 ],
                "visible": false

            },
            {
                "targets": [ -1 ],
                "width": "20%",
                'className': 'dt-body-center',
                'render': function (data, type, full, meta){
                    return '<input type="checkbox">';
                }
            },
        ]

    });


    studentRosterTable = $('#student_roster').DataTable({
        "bPaginate": false,
        "bFilter": false,
        "bLengthChange": false,
        "bSort" : false,
    });

    $("#deacivateProblemSets").click(function () {
        var rows = $("#activateProbSetTable").dataTable().fnGetNodes();
        var rowsArray = [];
        var activateData = [];
        var i = 0;
        $("input:checkbox:not(:checked)",rows).each(function(){
            rowsArray[i] = $(this).closest('tr');
            i++;
        });
        for(var j=0; j < rowsArray.length; j++)
            activateData[j]  =  $("#activateProbSetTable").DataTable().row( rowsArray [j] ).data()[3];

        $.ajax({
            type : "POST",
            url :pgContext+"/tt/tt/configureProblemSets",
            data : {
                activateData: activateData,
                classid: classID,
                activateFlag: 'deactivate'
            },
            success : function(response) {
                if (response.includes("***")) {
                    $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                    $('#errorMsgModelPopup').modal('show');
                }else{
                    $("#successMsgModelPopupForProblemSets").find("[class*='modal-body']").html( "Selected problemsets are deactivated" );
                    $('#successMsgModelPopupForProblemSets').modal('show');
                }
            }
        });

    });

    $('#createMoreStudentId').click(function () {

        var dataForm = $("#create_Student_id").serializeArray();
        var values=[];
        $.each(dataForm, function(i, field){
            values[i] = field.value;
        });

        $.ajax({
            type: "POST",
            url: pgContext + "/tt/tt/createMoreStudentIds",
            data: {
                formData: values
            },
            success: function (data) {
                console.log("SUCCESS: ", data);
                if (data.includes("***")) {
                    $("#errorMsgModelPopup").find("[class*='modal-body']").html( data );
                    $('#errorMsgModelPopup').modal('show');
                }else{
                    $("#successMsgModelPopup").find("[class*='modal-body']").html( "User creation was successful. Please refresh you page to view your change " );
                    $('#successMsgModelPopup').modal('show');
                }

            }
        });

    });


    $("#acivateProblemSets").click(function () {
        var rows = $("#inActiveProbSetTable").dataTable().fnGetNodes();
        var rowsArray = [];
        var activateData = [];
        var i = 0;
        $("input:checked", rows).each(function(){
            rowsArray[i] = $(this).closest('tr');
            i++;
        });
        for(var j=0; j < rowsArray.length; j++)
            activateData[j]  = $("#inActiveProbSetTable").DataTable().row( rowsArray [j] ).data()[3];

        $.ajax({
            type : "POST",
            url :pgContext+"/tt/tt/configureProblemSets",
            data : {
                activateData: activateData,
                classid: classID,
                activateFlag: 'activate'
            },
            success : function(response) {
                if (response.includes("***")) {
                    $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                    $('#errorMsgModelPopup').modal('show');
                }else{
                    $("#successMsgModelPopupForProblemSets").find("[class*='modal-body']").html( "Selected problemsets are activated" );
                    $('#successMsgModelPopupForProblemSets').modal('show');
                }
            }
        });

    });

    $("#successMsgModelPopupForProblemSets").find("[class*='btn btn-default']").click(function () {
            location.reload();
    });
    $("#successMsgModelPopupForProblemSets").find("[class*='close']").click(function () {
        location.reload();
    });


    $(".active").click(function () {
        $(this).children(':first').toggleClass('rotate-icon');
        var tr = $(this).closest('tr');
        var row = activetable.row( tr );

        if ( row.child.isShown() ) {
            row.child.hide();
        }else{
            var rowID = '#'+row.data()[0];
            $.ajax({
                type : "POST",
                url :pgContext+"/tt/tt/getProblemForProblemSets",
                data : {
                    problemID: row.data()[3],
                    classid: classID
                },
                success : function(response) {
                    if (response.includes("***")) {
                        $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                        $('#errorMsgModelPopup').modal('show');
                    }else {
                        var child = problemDetails(row.data(), response);
                        row.child(child).show();
                        $(rowID).toggleClass('zoomIn zoomOut');
                    }
                }
            });

        }
    });

    $(".passive").click(function () {
        $(this).children(':first').toggleClass('rotate-icon');
        var tr = $(this).closest('tr');
        var row = inactivetable.row( tr );

        if ( row.child.isShown() ) {
            row.child.hide();
        }else{
            var rowID = '#'+row.data()[0];
            $.ajax({
                type : "POST",
                url :pgContext+"/tt/tt/getProblemForProblemSets",
                data : {
                    problemID: row.data()[3],
                    classid: classID
                },
                success : function(response) {
                    if (response.includes("***")) {
                        $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                        $('#errorMsgModelPopup').modal('show');
                    }else {
                        var child = problemDetails(row.data(), response);
                        row.child(child).show();
                        $(rowID).toggleClass('zoomIn zoomOut');
                    }
                }
            });

        }
    });
    var myLineChart;
    $('body').on('click', 'div.getMastery-trajectory-for-problemset', function () {

        var topicId = $(this).find("span").text();
        var td = $(this).closest('td');
        var bgcolor = "#BDB7B5"

        if(td.attr('class')){
            if(td.attr('class') == 'span-danger-layer-one')
                bgcolor = '#FF4766'
            else if(td.attr('class') == 'span-warning-layer-one')
                bgcolor = '#FFB647'
            else if(td.attr('class') == 'span-info-layer-one')
                bgcolor = '#33b5e5'
            else
                bgcolor = '#00C851'

        }
        var tr = $(this).closest('tr');
        var row = perProblemSetReport.row(tr);
        var topicId = $(this).find("span").text();
        var studentId = row.data()['studentId'];

        $.ajax({
            type: "POST",
            url: pgContext + "/tt/tt/getMasterProjectionsForCurrentTopic",
            data: {
                classId: classID,
                topicID: topicId,
                studentId: studentId
            },
            success: function (response) {
                var masteryProjectionsForThisTopic = $.parseJSON(response);
                var problemsMap = {};
                var chartLabel = [];
                var chartData = [];
                var perProblemSetLevelOneFullTemp = [];
                if(myLineChart) {
                    myLineChart.destroy();
                }
                masteryProjectionsForThisTopic.forEach(function (e) {
                    var perProblemSetLevelOneTemp = {};
                    perProblemSetLevelOneTemp['problemId'] = e[0];
                    perProblemSetLevelOneTemp['problemName'] = e[1];
                    perProblemSetLevelOneTemp['studentEffort'] =e[7];

                    problemsMap[e[1]] =  e[4];
                    chartLabel.push(e[0]);
                    chartData.push(e[5]);

                    perProblemSetLevelOneFullTemp.push(perProblemSetLevelOneTemp);
                });


                var columDvalues = [{data : "problemId"},{data : "problemName"},{data : "studentEffort"}]
                var columNvalues = [{"title" : "Problem ID", "targets": [0]},{"title" : "Problem Name", "targets": [1], "render": function ( data, type, row ) {
                    return  "<a style='cursor:pointer' rel='popover' data-img='" + problemsMap[data] + "'>" + data + "</a>";
                }
                },{"title" : "Student Effort", "targets": [2], "render": function ( data, type, row ) {
                    return  "<a style='cursor:pointer' rel='popoverLabel' data-content='"+effortLabelMap[data]+"'>" + data + "</a>";
                }
                }];

                var  masteryTrajecotoryLegend = $('#masteryTrajecotoryLegend').DataTable({
                    data: perProblemSetLevelOneFullTemp,
                    destroy: true,
                    "scrollCollapse": true,
                    "bInfo": false,
                    "columns" : columDvalues,
                    "columnDefs" : columNvalues,
                    "bFilter": false,
                    "bLengthChange": false,
                    rowReorder: false,
                    "bSort" : false ,
                    "drawCallback": function() {
                        $('a[rel=popover]').popover({
                            html: true,
                            trigger: 'hover',
                            placement: 'right',
                            content: function () {
                                return '<img src="' + $(this).data('img') + '" />';
                            }
                        });
                        $('a[rel=popoverLabel]').popover({
                            html: false,
                            trigger: 'hover',
                            placement: 'right',
                        });
                    }
                });

                myLineChart = new Chart($("#masteryTrajectoryReportCanvas"), {
                    type: 'line',
                    data: {
                        labels: chartLabel,
                        datasets: [{
                            label: 'Mastery Recorded',
                            data: chartData,
                            backgroundColor: bgcolor
                        }]
                    }, options: {
                        scales: {
                            xAxes: [{
                                scaleLabel: {
                                    display: true,
                                    labelString: 'Problems Seen in Order'
                                }
                            }],
                            yAxes: [{
                                display: true,
                                ticks: {
                                    suggestedMin: 0.00,
                                    max: 1.00
                                }
                            }]
                        } ,
                        legend: {
                            display: false,
                            position: 'bottom',
                        }
                    }
                });
                $('#masteryTrajectoryReport').modal('show');

            }
        });
    });


    $('body').on('click', 'a.viewEachStudentDetail', function () {
        $(this).children(':first').toggleClass('rotate-icon');
        var tr = $(this).closest('tr');
        var row = perStudentReport.row(tr);
        var rowID = row.data()[0];
        var containerChartSelector = "#containerChart" + rowID;
        var legendChart = "#legendChart" + rowID;
        $(containerChartSelector).hide();
        $("#iconID" + rowID).show();
        $(legendChart).empty();

        if (row.child.isShown()) {
            row.child.hide();
        } else {
            var tableHeader = '<div id='+"panel"+rowID+' class="panel-body animated zoomOut"><table id='+rowID+' class="table table-striped table-bordered" cellspacing="0" width="100%"><thead><tr><th>Problem</th><th>Problem Nickname</th><th>Problem finished on</th><th>Problem Description</th><th>Solved Correctly</th><th># of mistakes made</th><th># of hints seen</th><th># of attempts made</th><th>Effort</th></tr></thead><tbody>';
            var studentDataList = eachStudentData[rowID];
            var outputStudentDataList = Object.keys(studentDataList).map(function(key) {return studentDataList[key];});
            outputStudentDataList.sort(function(a,b) {
                if(a[10] == 'Problem was not completed')
                    return new Date('1900-01-01 00:00:01.0').getTime() - new Date('1900-01-01 00:00:00.0').getTime();
                if( b[10] == 'Problem was not completed' )
                    return new Date('1900-01-01 00:00:00.0').getTime() - new Date('1900-01-01 00:00:01.0').getTime();
                else
                return new Date(b[10]).getTime() - new Date(a[10]).getTime();
            });

            $.each(outputStudentDataList, function (i, obj) {
                var correctHtml = "";
                var problemImgHTML = "<td> <a style='cursor:pointer' rel='popover' data-img='" + obj[3] + "'>" + obj[0] + "</a></td>"
                var effortLabelHTML = "<td> <a style='cursor:pointer' rel='popoverLabel' data-content='"+effortLabelMap[obj[8]]+"'>" + obj[8] + "</a></td>"
                if ("1" == obj[4])
                    correctHtml = "<td><img style='width:15%;' src='"+servletContextPath+"/images/check.png'/></td>";
                else
                    correctHtml = "<td><img style='width:15%;' src='"+servletContextPath+"/images/x.png'/></td>";

                tableHeader += "<tr>" + problemImgHTML + "<td>" + obj[1] + "</td><td>" +obj[10]+ "</td><td>" + obj[2] + "</td>" + correctHtml + "<td>" + obj[5] + "</td><td>" + obj[6] + "</td><td>" + obj[7] + "</td>"+effortLabelHTML+"</tr>";

            });
            tableHeader += "</tbody></table></div>"
            row.child(tableHeader).show();
            $("#panel"+rowID).toggleClass('zoomIn zoomOut');
            $('a[rel=popover]').popover({
                html: true,
                trigger: 'hover',
                placement: 'right',
                content: function () {
                    return '<img src="' + $(this).data('img') + '" />';
                }
            });
            $('a[rel=popoverLabel]').popover({
                html: false,
                trigger: 'hover',
                placement: 'left',
            });

        }

    });
    function testMethod() {
        console.log("a");
    }
var completeDataChart;
    $(document).on('click', 'a.getCompleteMasteryByAverage', function () {
        var tr = $(this).closest('tr');
        var row = perProblemSetReport.row(tr);
        var studentID = row.data()['studentId'];
        $.ajax({
            type: "POST",
            url: pgContext + "/tt/tt/getCompleteMasteryProjectionForStudent",
            data: {
                classId: classID,
                chartType: 'avg',
                studentId: studentID
            },
            success: function (response) {
                var completeProjectionByAVG = $.parseJSON(response);
                var problemsetName = [];
                var masteryData = [];
                if(completeDataChart) {
                    completeDataChart.destroy();
                }
                $.each( completeProjectionByAVG, function (i, obj) {
                    var tmp = obj.split("~~~");
                    problemsetName.push(perProblemSetColumnNamesMap[tmp[0]]);
                    masteryData.push(tmp[1]);
                });

                completeDataChart = new Chart($("#completeMasteryForStudentCanvas"), {
                    type: 'bar',
                    data: {
                        labels: problemsetName,
                        datasets: [{
                            label: 'Average Mastery Recorded',
                            data: masteryData,
                            backgroundColor: '#33b5e5'
                        }]
                    }, options: {
                        legend: {
                            display: false,
                            position: 'bottom'
                        },scales: {
                            yAxes: [{
                                display: true,
                                ticks: {
                                    suggestedMin: 0.00,
                                    max: 1.00
                                }
                            }]
                        }
                    }
                });
                $('#completeMasteryForStudent').modal('show');
            }
        });
    });

    $(document).on('click', 'a.getCompleteMasteryByMax', function () {
        var tr = $(this).closest('tr');
        var row = perProblemSetReport.row(tr);
        var studentID = row.data()['studentId'];
        $.ajax({
            type: "POST",
            url: pgContext + "/tt/tt/getCompleteMasteryProjectionForStudent",
            data: {
                classId: classID,
                chartType: 'max',
                studentId: studentID
            },
            success: function (response) {
                var completeProjectionByMax = $.parseJSON(response);
                var problemsetName = [];
                var masteryData = [];
                if(completeDataChart) {
                    completeDataChart.destroy();
                }
                $.each( completeProjectionByMax, function (i, obj) {
                    var tmp = obj.split("~~~");
                    problemsetName.push(perProblemSetColumnNamesMap[tmp[0]]);
                    masteryData.push(tmp[1]);
                });

                completeDataChart = new Chart($("#completeMasteryForStudentCanvas"), {
                    type: 'bar',
                    data: {
                        labels: problemsetName,
                        datasets: [{
                            label: 'Max Mastery Recorded',
                            data: masteryData,
                            backgroundColor: '#33b5e5'
                        }]
                    }, options: {
                        legend: {
                            display: false,
                            position: 'bottom'
                        },scales: {
                            yAxes: [{
                                display: true,
                                ticks: {
                                    suggestedMin: 0.00,
                                    max: 1.00
                                }
                            }]
                        }
                    }
                });
                $('#completeMasteryForStudent').modal('show');
            }
        });

    });

    $(document).on('click', 'a.getCompleteMasteryByLatest', function () {
        var tr = $(this).closest('tr');
        var row = perProblemSetReport.row(tr);
        var studentID = row.data()['studentId'];
        $.ajax({
            type: "POST",
            url: pgContext + "/tt/tt/getCompleteMasteryProjectionForStudent",
            data: {
                classId: classID,
                chartType: 'latest',
                studentId: studentID
            },
            success: function (response) {
                var completeProjectionByLatest = $.parseJSON(response);
                var problemsetName = [];
                var masteryData = [];
                if(completeDataChart) {
                    completeDataChart.destroy();
                }
                $.each( completeProjectionByLatest, function (i, obj) {
                    var tmp = obj.split("~~~");
                    problemsetName.push(perProblemSetColumnNamesMap[tmp[0]]);
                    masteryData.push(tmp[1]);
                });

                completeDataChart = new Chart($("#completeMasteryForStudentCanvas"), {
                    type: 'bar',
                    data: {
                        labels: problemsetName,
                        datasets: [{
                            label: 'Last Recorded Mastery',
                            data: masteryData,
                            backgroundColor: '#33b5e5'
                        }]
                    }, options: {
                        legend: {
                            display: false,
                            position: 'bottom'
                        },scales: {
                            yAxes: [{
                                display: true,
                                ticks: {
                                    suggestedMin: 0.00,
                                    max: 1.00
                                }
                            }]
                        }
                    }
                });
                $('#completeMasteryForStudent').modal('show');
            }
        });

    });

    activetable.on( 'row-reorder', function ( e, diff, edit ) {
        activetable.$('input').removeAttr( 'checked' );
        var result = [];
        console.log(pgContext);
        for ( var i=0; i< diff.length ; i++ ) {
            var rowData = activetable.row( diff[i].node ).data();
            result[i] = rowData[3]+'~~'+ diff[i].newData+'~~'+diff[i].oldData;
        }
        $.ajax({
            type : "POST",
            url :pgContext+"/tt/tt/reOrderProblemSets",
            data : {
                problemSets: result,
                classid: classID
            },
            success : function(response) {
                if (response.includes("***")) {
                    $("#errorMsgModelPopup").find("[class*='modal-body']").html( response );
                    $('#errorMsgModelPopup').modal('show');
                }
            }
        });
    } );

    /** Report Handler Starts **/

    $('#collapseOne').on('show.bs.collapse', function ()  {
        $('#collapseOne').find('.loader').show();
        $.ajax({
            type : "POST",
            url : pgContext+"/tt/tt/getTeacherReports",
            data : {
                classId: classID,
                teacherId: teacherID,
                reportType: 'perStudentPerProblemSetReport'
            },
            success : function(data) {
                $('#collapseOne').find('.loader').hide();
                var jsonData = $.parseJSON(data);
                perProblemSetLevelOne = jsonData.levelOneData;
                perProblemSetColumnNamesMap = jsonData.columns;

                var indexcolumn = 3;
                var columNvalues = $.map(perProblemSetColumnNamesMap, function (v) {
                        var temp = {
                            "title": v, "name": v.replace(/\s/g, ''), "targets": indexcolumn,
                            "createdCell": function (td, cellData, rowData, row, col) {
                                if (cellData == '') {
                                    $(td).text();
                                    return;
                                }
                                var dataArray = cellData.split("---");
                                $(td).html(""+dataArray[0] + dataArray[1]+"&nbsp;&nbsp;<div class='fa fa-line-chart getMastery-trajectory-for-problemset' title='Get Mastery Trajectory' style='cursor: pointer;' aria-hidden='true'><span style='display: none'>"+dataArray[3]+"</span></div>");
                                if (dataArray[1] <= 0.25) {
                                    if (dataArray[2] >= 10) {
                                        $(td).addClass('span-danger-layer-one');
                                    }

                                } else if (dataArray[1] > 0.25 && dataArray[1] < 0.5) {
                                    if (dataArray[2] >= 10) {
                                        $(td).addClass('span-warning-layer-one');
                                    }
                                } else if (dataArray[1] > 0.5 && dataArray[1] < 0.75) {
                                    if (dataArray[2] >= 10) {
                                        $(td).addClass('span-info-layer-one');
                                    }
                                } else if (dataArray[1] > 0.75) {
                                    if (dataArray[2] >= 10) {
                                        $(td).addClass('span-sucess-layer-one');
                                    }
                                }
                            }
                        };
                        indexcolumn++;
                        return temp;
                    }
                );
                columNvalues.unshift({"title" : "Student Name","name":"studentName" , "targets": [0]},
                    {"title" : "Username","name":"userName", "targets": [1],   "createdCell": function (td, cellData, rowData, row, col) {
                        $(td).html(cellData+"&nbsp;&nbsp;" +
                            "<a tabindex='0' rel='completeMasteryChartPopover' data-toggle='popover' data-trigger='focus' title='Get Complete Mastery Chart' style='cursor: pointer;' aria-hidden='true'><i class='fa fa-bar-chart' aria-hidden='true'/></a>");
                        }
                    },
                    {"title" : "StudentID","name":"studentId", "targets": [2], visible : false});
                var columDvalues = $.map(perProblemSetColumnNamesMap, function(v) {
                        v = v.replace(/\s/g, '');
                        return  { width: "20%", data : v };
                    }
                );
                columDvalues.unshift({data: "studentName"},{data: "userName"},{data: "studentId"});
                var perProblemSetLevelOneFullTemp = [];
                $.map(perProblemSetLevelOne, function (item,k) {
                        var perProblemSetLevelOneTemp = {};
                                    item.forEach(function(e){
                                        var itemArrays = e.split("~~~");
                                        perProblemSetLevelOneTemp[itemArrays[0]] = itemArrays[1]
                                        perProblemSetLevelOneTemp['studentId'] = k;
                                })
                        perProblemSetLevelOneFullTemp.push(perProblemSetLevelOneTemp);
                    }
                );

                if (perProblemSetReport) {
                    perProblemSetReport.destroy();
                    $('#perTopicStudentReport').empty();
                }

                perProblemSetReport = $('#perTopicStudentReport').DataTable({
                    data: perProblemSetLevelOneFullTemp,
                    destroy: true,
                    "columns": columDvalues,
                    "columnDefs": columNvalues,
                    "bPaginate": true,
                    "scrollX": true,
                    "bFilter": false,
                    "bLengthChange": false,
                    rowReorder: false,
                    "bSort": false,
                    "drawCallback": function () {
                        $('a[rel=completeMasteryChartPopover]').popover({
                            html: true,
                            trigger: 'focus',
                            placement: 'right',
                            content: function () {
                                return '<ul><li><a style="cursor: pointer;" class="getCompleteMasteryByAverage"> Get Complete "Mastery" by average </a></li>' +
                                    '<li><a style="cursor: pointer;" class="getCompleteMasteryByMax"> Get "Mastery" reported by highest recorded value for problemset</a></li>' +
                                    '<li><a style="cursor: pointer;" class="getCompleteMasteryByLatest"> Get Complete "Mastery" by latest recorded value for each problemset</a></li></ul>';
                            }
                        })
                    }
                });

            }
        });


    });

    $('#collapseTwo').on('show.bs.collapse', function ()  {
        $('#collapseTwo').find('.loader').show();
        $.ajax({
            type : "POST",
            url : pgContext+"/tt/tt/getTeacherReports",
            data : {
                classId: classID,
                teacherId: teacherID,
                reportType: 'perProblemReport'
            },
            success : function(data) {
                $('#collapseTwo').find('.loader').hide();
                var jsonData = $.parseJSON(data);
                var eachProblemData = jsonData.levelOneDataPerProblem;
                var perProblemSetLevelOneFullTemp = [];
                var problemImageMap = [];
                var problemImageWindow = [];
                $.map(eachProblemData, function (item, key) {
                    var perProblemSetLevelOneTemp = {};
                    perProblemSetLevelOneTemp['problemId'] = key;
                    $.map(item, function (itemValues, k) {
                        if (k == 'problemName' || k == 'noStudentsSeenProblem' ||
                            k == 'getGetPercStudentsSolvedFirstTry' || k == 'getGetPercStudentsSolvedSecondTry' || k == 'percStudentsRepeated' ||
                            k == 'percStudentsSkipped' || k == 'percStudentsGaveUp' || k == 'mostIncorrectResponse' || k=='problemStandardAndDescription') {
                            perProblemSetLevelOneTemp[k] = itemValues;
                            }else if(k=='imageURL'){
                            problemImageMap[key] = itemValues;
                            }else if(k == 'problemURLWindow'){
                            problemImageWindow[key]  = itemValues;
                            }
                        });
                        perProblemSetLevelOneFullTemp.push(perProblemSetLevelOneTemp);
                        });
                var columNvalues = [
                    { "title": "Problem ID", "name" : "problemId" , "targets" : [0]},
                    { "title": "Problem Name", "name" : "problemName" , "targets" : [1],"render": function ( data, type, full, meta ) {
                            var problemId = full['problemId'];
                            var attri = ", 'ProblemPreview'"+","+"'width=750,height=550,status=yes,resizable=yes'";
                             var window = "'" + problemImageWindow[problemId] + "'" + attri ;
                            return '<a  onclick="window.open('+window+');" style="cursor:pointer" rel="popoverPerProblem" data-img="' + problemImageMap[problemId] + '">' + data + '</a>';
                    }},
                    { "title": "CC Standard", "name" : "problemStandardAndDescription" , "targets" : [2],"render": function ( data, type, full, meta ) {
                        var standardSplitter = data.split(":");
                        return "<a style='cursor:pointer' rel='popoverstandard' data-content='" + standardSplitter[1]+ "'>" + standardSplitter[0] + "</a>";
                    }},
                    { "title": "# of Students seen the problem", "name" : "noStudentsSeenProblem","targets" : [3] },
                    { "title": "% of Students solved the problem on the first attempt", "name" : "getGetPercStudentsSolvedFirstTry","targets" : [4] ,"render": function ( data, type, full, meta ) {
                        return data+" %";
                    },"createdCell": function (td, cellData, rowData, row, col) {
                            if(cellData >= 80){
                                $(td).html(cellData +"&nbsp;&nbsp;<i class='fa fa-thumbs-up' aria-hidden='true'></i>");
                            }else if(cellData <= 20){
                                $(td).addClass('span-danger-layer-one');
                            }
                    } },
                    { "title": "# of Students solved the problem on the second attempt", "name" : "getGetPercStudentsSolvedSecondTry","targets" : [5], visible : false },
                    { "title": "% of Students repeated the problem", "name" : "percStudentsRepeated","targets" : [6],"render": function ( data, type, full, meta ) {
                        return data+" %";
                    }},
                    { "title": "% of Students skipped the problem", "name" : "percStudentsSkipped","targets" : [7] ,"render": function ( data, type, full, meta ) {
                        return data+" %";
                    }},
                    { "title": "% of Students gave up", "name" : "percStudentsGaveUp","targets" : [8],"render": function ( data, type, full, meta ) {
                        return data+" %";
                    }},
                    { "title": "Most Frequent Incorrect Response", "name" : "mostIncorrectResponse","targets" : [9] }
                ];
                var columDvalues = [
                    { width: "10%", data : "problemId"},
                    { width: "10%", data : "problemName" },
                    { width: "10%", data : "problemStandardAndDescription" },
                    { width: "10%", data : "noStudentsSeenProblem" },
                    { width: "10%", data : "getGetPercStudentsSolvedFirstTry" },
                    { width: "5%", data : "getGetPercStudentsSolvedSecondTry"},
                    { width: "10%", data : "percStudentsRepeated"},
                    {width: "10%", data : "percStudentsSkipped"},
                    { width: "10%", data : "percStudentsGaveUp"},
                    { width: "10%", data : "mostIncorrectResponse"}
                ];

                if (perProblemReportTable) {
                    perProblemReportTable.destroy();
                    $('#perProblemReport').empty();
                }

                perProblemReportTable = $('#perProblemReport').DataTable({
                    data: perProblemSetLevelOneFullTemp,
                    destroy: true,
                    "columns": columDvalues,
                    "columnDefs": columNvalues,
                    "bPaginate": true,
                    "scrollX": true,
                    "bFilter": false,
                    "bLengthChange": false,
                    rowReorder: false,
                    "bSort": true,
                    "order": [[ 3, "desc" ]],
                    "drawCallback": function () {
                        $('a[rel=popoverPerProblem]').popover({
                            html: true,
                            trigger: 'hover',
                            placement: 'right',
                            container: 'body',
                            content: function () {
                                return '<img src="' + $(this).data('img') + '" />';
                            }
                        });

                        $('a[rel=popoverstandard]').popover({
                            html: false,
                            trigger: 'hover',
                            placement: 'right',
                            container: 'body',
                        });
                        $('a[rel=popoverHeader]').popover({
                            container : 'body',
                            trigger : 'hover',
                            placement: 'top',
                        });

                    },
                    headerCallback: function headerCallback(thead, data, start, end, display) {
                       $(thead).find('th').eq(5).html('% of Students repeated the problem &nbsp;&nbsp;<a rel="popoverHeader"  data-content="Students who received the problem again, because the last time they did NOT solve it."><i class="fa fa-question-circle-o" aria-hidden="true"></i></a>');
                       $(thead).find('th').eq(6).html('% of Students skipped the problem &nbsp;&nbsp;<a rel="popoverHeader" data-content="Students who received the problem and immediately clicked the '+"New Problem"+' button"><i class="fa fa-question-circle-o" aria-hidden="true"></i></a>');
                       $(thead).find('th').eq(7).html('% of Students gave up &nbsp;&nbsp;<a rel="popoverHeader" data-content="Students who started working on the problem, but decided to move on to another one (quit)."><i class="fa fa-question-circle-o" aria-hidden="true"></i></a>');

                    }
                });


            }

        });

    });

    $('#collapseThree').on('show.bs.collapse', function ()  {
        $.ajax({
            type : "POST",
            url : pgContext+"/tt/tt/getTeacherReports",
            data : {
                classId: classID,
                teacherId: teacherID,
                reportType: 'perStudentReport'
            },
            success : function(data) {
                var jsonData = $.parseJSON(data);
                effortMap = jsonData.effortChartValues;
                eachStudentData = jsonData.eachStudentDataValues;
                perStudentReport.clear().draw();
                perStudentReport.rows.add(jsonData.levelOneData).draw();
                perStudentReport.columns.adjust().draw();
            },
            error : function(e) {
                console.log(e);
            }
        });

    });

    $('#collapseFour').on('show.bs.collapse', function ()  {
        $('#collapseFourLoader').show();
        $.ajax({
            type : "POST",
            url : pgContext+"/tt/tt/getTeacherReports",
            data : {
                classId: classID,
                teacherId: teacherID,
                reportType: 'commonCoreClusterReport'
            },
            success : function(data) {
                $('#collapseFourLoader').hide();
                var jsonData = $.parseJSON(data);
                var columNvalues = [
                    { "title": "Cluster's in Class", "name" : "clusterNames" , "targets" : [0],"render": function ( data, type, full, meta ) {
                        var clusterCCName = full['clusterCCName'];
                        return "<a style='cursor:pointer' rel='popoverCluster' data-content='"+clusterCCName+"'>" + data + "</a>";;
                    },"createdCell": function (td, cellData, rowData, row, col) {
                        if (rowData['noOfProblemsonFirstAttempt'] < 20 && rowData['totalHintsViewedPerCluster'] > 1.50) {
                            $(td).addClass('span-danger-layer-one');
                        } else if (rowData['noOfProblemsonFirstAttempt'] > 20 && rowData['noOfProblemsonFirstAttempt'] <  40 && rowData['totalHintsViewedPerCluster'] > 1.00) {
                            $(td).addClass('span-warning-layer-one');
                        }
                    }},
                    { "title": "# of problems in cluster", "name" : "noOfProblemsInCluster" , "targets" : [1],"render": function ( data, type, full, meta ) {
                        return '<label style="width: 50%;">'+data+'</label><a  class="getProblemDetailsPerCluster" aria-expanded="true" aria-controls="collapseOne"><i class="glyphicon glyphicon-menu-down"></i></a>';
                    },"createdCell": function (td, cellData, rowData, row, col) {
                        if (rowData['noOfProblemsonFirstAttempt'] < 20 && rowData['totalHintsViewedPerCluster'] > 1.50) {
                            $(td).addClass('span-danger-layer-one');
                        } else if (rowData['noOfProblemsonFirstAttempt'] > 20 && rowData['noOfProblemsonFirstAttempt'] <  40 && rowData['totalHintsViewedPerCluster'] > 1.00) {
                            $(td).addClass('span-warning-layer-one');
                        }
                    }},
                    { "title": "% solved in the first attempt", "name" : "noOfProblemsonFirstAttempt","targets" : [2],"createdCell": function (td, cellData, rowData, row, col) {
                        if (rowData['noOfProblemsonFirstAttempt'] < 20 && rowData['totalHintsViewedPerCluster'] > 1.50) {
                            $(td).addClass('span-danger-layer-one');
                        } else if (rowData['noOfProblemsonFirstAttempt'] > 20 && rowData['noOfProblemsonFirstAttempt'] <  40 && rowData['totalHintsViewedPerCluster'] > 1.00) {
                            $(td).addClass('span-warning-layer-one');
                        }
                    } },
                    { "title": "Avg ratio of hint requested", "name" : "totalHintsViewedPerCluster","targets" : [3],"createdCell": function (td, cellData, rowData, row, col) {
                        if (rowData['noOfProblemsonFirstAttempt'] < 20 && rowData['totalHintsViewedPerCluster'] > 1.50) {
                            $(td).addClass('span-danger-layer-one');
                        } else if (rowData['noOfProblemsonFirstAttempt'] > 20 && rowData['noOfProblemsonFirstAttempt'] <  40 && rowData['totalHintsViewedPerCluster'] > 1.00) {
                            $(td).addClass('span-warning-layer-one');
                        }
                    }},
                    { "title": "ClusterID", "name" : "clusterId","targets" : [4], visible : false},
                    { "title": "Cluster Description", "name" : "clusterCCName","targets" : [5], visible : false}
                ];
                var columDvalues = [
                    { width: "30%", data : "categoryCodeAndDisplayCode"},
                    { width: "20%", data : "noOfProblemsInCluster" },
                    { width: "20%", data : "noOfProblemsonFirstAttempt" },
                    { width: "20%", data : "totalHintsViewedPerCluster" },
                    { width: "5%", data : "clusterId"},
                    { width: "5%", data : "clusterCCName"}

                ];
                var dataPerCluster = [];
                $.map(jsonData, function (item, key) {
                    var perProblemSetLevelOneTemp = {};
                    $.map(item, function (itemValues, k)
                    {
                        perProblemSetLevelOneTemp[k] = itemValues;
                    });
                    dataPerCluster.push(perProblemSetLevelOneTemp);
                });

                if (perClusterReportTable) {
                    perClusterReportTable.destroy();
                    $('#perClusterReport').empty();
                }

                perClusterReportTable = $('#perClusterReport').DataTable({
                    data: dataPerCluster,
                    destroy: true,
                    "columns": columDvalues,
                    "columnDefs": columNvalues,
                    "bFilter": false,
                    "bLengthChange": false,
                    rowReorder: false,
                    "bSort": true,
                    "drawCallback": function () {
                        $('a[rel=popoverCluster]').popover({
                            html: false,
                            trigger: 'hover',
                            placement: 'right',
                            container: 'body',
                        });
                    }
                });
            }
        });

    });


    /** Report Handler Ends **/

    $('body').on('click', 'a.getProblemDetailsPerCluster', function () {
        $(this).children(':first').toggleClass('rotate-icon');
        var tr = $(this).closest('tr');
        var row = perClusterReportTable.row(tr);
        if ( row.child.isShown() ) {
            row.child.hide();
        }else {
            var clusterId = row.data()['clusterId'];

            $.ajax({
                type: "POST",
                url: pgContext + "/tt/tt/getProblemDetailsPerCluster",
                data: {
                    classId: classID,
                    teacherId: teacherID,
                    clusterId: clusterId
                },
                success: function (data) {
                    var jsonData = $.parseJSON(data);
                    var eachProblemData = jsonData;
                    var perProblemSetLevelOneFullTemp = [];
                    var problemImageMap = [];
                    var problemImageWindow = [];
                    $.map(eachProblemData, function (item, key) {
                        var perProblemSetLevelOneTemp = {};
                        perProblemSetLevelOneTemp['problemId'] = key;
                        $.map(item, function (itemValues, k) {
                            if (k == 'problemName' || k == 'noStudentsSeenProblem' ||
                                k == 'getGetPercStudentsSolvedFirstTry' || k == 'getGetPercStudentsSolvedSecondTry' || k == 'percStudentsRepeated' ||
                                k == 'percStudentsSkipped' || k == 'percStudentsGaveUp' || k == 'mostIncorrectResponse' || k == 'problemStandardAndDescription') {
                                perProblemSetLevelOneTemp[k] = itemValues;
                            } else if (k == 'imageURL') {
                                problemImageMap[key] = itemValues;
                            }else if(k == 'problemURLWindow'){
                                problemImageWindow[key]  = itemValues;
                            }

                        });
                        perProblemSetLevelOneFullTemp.push(perProblemSetLevelOneTemp);
                    });
                    var columNvalues = [
                        {"title": "Problem ID", "name": "problemId", "targets": [0]},
                        {
                            "title": "Problem Name",
                            "name": "problemName",
                            "targets": [1],
                            "render": function (data, type, full, meta) {
                                var problemId = full['problemId'];
                                var attri = ", 'ProblemPreview'"+","+"'width=750,height=550,status=yes,resizable=yes'";
                                var window = "'" + problemImageWindow[problemId] + "'" + attri ;
                                return '<a  onclick="window.open('+window+');" style="cursor:pointer" rel="popoverPerProblem" data-img="' + problemImageMap[problemId] + '">' + data + '</a>';
                            }
                        },
                        {
                            "title": "CC Standard",
                            "name": "problemStandardAndDescription",
                            "targets": [2],
                            "render": function (data, type, full, meta) {
                                var standardSplitter = data.split(":");
                                return "<a style='cursor:pointer' rel='popoverstandard' data-content='" + standardSplitter[1] + "'>" + standardSplitter[0] + "</a>";
                            }
                        },
                        {"title": "# of Students seen the problem", "name": "noStudentsSeenProblem", "targets": [3]},
                        {
                            "title": "% of Students solved the problem on the first attempt",
                            "name": "getGetPercStudentsSolvedFirstTry",
                            "targets": [4],
                            "createdCell": function (td, cellData, rowData, row, col) {
                                if(cellData >= 80){
                                    $(td).html(cellData +"&nbsp;&nbsp;<i class='fa fa-thumbs-up' aria-hidden='true'></i>");
                                }else if(cellData <= 20){
                                    $(td).addClass('span-danger-layer-one');
                                }
                            },"render": function ( data, type, full, meta ) {
                            return data+" %";
                        }
                        },
                        {
                            "title": "# of Students solved the problem on the second attempt",
                            "name": "getGetPercStudentsSolvedSecondTry",
                            "targets": [5],
                            visible: false
                        },
                        {
                            "title": "% of Students repeated the problem",
                            "name": "percStudentsRepeated",
                            "targets": [6],
                            "render": function ( data, type, full, meta ) {
                            return data+" %";
                        }
                        },
                        {
                            "title": "% of Students skipped the problem",
                            "name": "percStudentsSkipped",
                            "targets": [7],
                            "render": function ( data, type, full, meta ) {
                            return data+" %";
                        }
                        },
                        {
                            "title": "% of Students gave up",
                            "name": "percStudentsGaveUp",
                            "targets": [8],
                            "render": function ( data, type, full, meta ) {
                            return data+" %";
                        }
                        },
                        {"title": "Most Frequent Incorrect Response", "name": "mostIncorrectResponse", "targets": [9]}
                    ];

                    var columDvalues = [
                        {width: "10%", data: "problemId"},
                        {width: "10%", data: "problemName"},
                        {width: "10%", data: "problemStandardAndDescription"},
                        {width: "10%", data: "noStudentsSeenProblem"},
                        {width: "10%", data: "getGetPercStudentsSolvedFirstTry"},
                        {width: "5%", data: "getGetPercStudentsSolvedSecondTry"},
                        {width: "10%", data: "percStudentsRepeated"},
                        {width: "10%", data: "percStudentsSkipped"},
                        {width: "10%", data: "percStudentsGaveUp"},
                        {width: "10%", data: "mostIncorrectResponse"}
                    ];


                    var $perClusterChildtable = $($('#child_table_perCluster').html());
                    $perClusterChildtable.css('width', '100%');

                    var $perClusterChildtableLegend = $($('#child_table_legendCluster').html());
                    $perClusterChildtableLegend.css('width', '40%');

                    var perClusterChildtableLegend = $perClusterChildtableLegend.DataTable({
                        "bPaginate": false,
                        "bFilter": false,
                        "bLengthChange": false,
                        rowReorder: false,
                        "bSort": false});

                    var perClusterChildtable = $perClusterChildtable.DataTable({
                        data: perProblemSetLevelOneFullTemp,
                        destroy: true,
                        "columns": columDvalues,
                        "columnDefs": columNvalues,
                        "bPaginate": true,
                        "bFilter": false,
                        "bLengthChange": false,
                        rowReorder: false,
                        "bSort": true,
                        "drawCallback": function () {
                            $('a[rel=popoverPerProblem]').popover({
                                html: true,
                                trigger: 'hover',
                                placement: 'right',
                                container: 'body',
                                content: function () {
                                    return '<img src="' + $(this).data('img') + '" />';
                                }
                            });

                            $('a[rel=popoverstandard]').popover({
                                html: false,
                                trigger: 'hover',
                                placement: 'right',
                                container: 'body',
                            });

                            $('a[rel=popoverHeader]').popover({
                                container: 'body',
                                trigger: 'hover',
                                placement: 'top',
                            });

                        },
                        headerCallback: function headerCallback(thead, data, start, end, display) {
                            $(thead).find('th').eq(5).html('% of Students repeated the problem &nbsp;&nbsp;<a rel="popoverHeader"  data-content="Students who received the problem again, because the last time they did NOT solve it."><i class="fa fa-question-circle-o" aria-hidden="true"></i></a>');
                            $(thead).find('th').eq(6).html('% of Students skipped the problem &nbsp;&nbsp;<a rel="popoverHeader" data-content="Students who received the problem and immediately clicked the '+"New Problem"+' button"><i class="fa fa-question-circle-o" aria-hidden="true"></i></a>');
                            $(thead).find('th').eq(7).html('% of Students gave up &nbsp;&nbsp;<a rel="popoverHeader" data-content="Students who started working on the problem, but decided to move on to another one (quit)."><i class="fa fa-question-circle-o" aria-hidden="true"></i></a>');

                        }
                    });
                    perClusterReportTable.row(tr).child(perClusterChildtable.table().container()).show();
                }
            });
        }

    });

}