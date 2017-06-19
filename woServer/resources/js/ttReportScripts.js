/**
 * Created by nsmenon on 6/1/2017.
 */
var perStudentReport;
var effortMap;
var eachStudentData = [];
var activetable;
var inactivetable;
var studentRosterTable;

var effortLabelMap = {"SKIP" : "The student did nothing and skipped the problem.",
                       "NOTR" : "The student made a first attempt to solve a problem in a time under 4 seconds –not enough time to even read the problem.",
                        "GIVEUP" : "The student took some action, but then skipped the problem without solving it.",
                        "SOF" :  "The student solved the problem on their first attempt, without seeing any help.",
                        "ATT" : "The student didn’t see any hints and solved it correctly after 1 wrong attempt.",
                        "GUESS" : "The student solved it correctly with no hints and more than 1 incorrect attempt.",
                        "SHINT" : "Student got the problem eventually right, with at least 1 hint.",
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
        labels: ["Student Efforts in the Class in %"],
        datasets: [{
            label: 'SKIP: The student did nothing and skipped the problem.',
            backgroundColor: "#8dd3c7",
            data: [effortValues[0]],
        },
            {
                label: 'NOTR: The student made a first attempt to solve a problem in a time under 4 seconds –not enough time to even read the problem.',
                backgroundColor: "#ffffb3",
                data: [effortValues[1]],
            },
            {
                label: 'GIVEUP: The student took some action, but then skipped the problem without solving it.',
                backgroundColor: "#bebada",
                data: [effortValues[2]],
            },
            {
                label: 'SOF: The student solved the problem on their first attempt, without seeing any help.',
                backgroundColor: "#fb8072",
                data: [effortValues[3]],
            },
            {
                label: 'ATT: The student didn’t see any hints and solved it correctly after 1 wrong attempt.',
                backgroundColor: "#b3de69",
                data: [effortValues[4]],
            },
            {
                label: 'GUESS: The student solved it correctly with no hints and more than 1 incorrect attempt.',
                backgroundColor: "#fccde5",
                data: [effortValues[5]],
            },
            {
                label: 'SHINT: Student got the problem eventually right, with at least 1 hint.',
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
    $.ajax({
        type : "POST",
        url :pgContext+"/tt/tt/resetStudentPassword",
        data : {
            studentId: id,
            userName: uname
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

        var tempStudentId = "<tr><td><label for='studentId'>UserId</label></td><td><input type='text' value="+id+" id='studentId' class='form-control' name='studentId' disabled='disabled' /></td></tr>";
        var tempStudentUserName = "<tr><td><label for='studentUsername'>Username</label></td><td><input type='text' value="+uname+" id='studentUsername' class='form-control' name='studentUsername'/></td></tr>";
        if(fname == ''){
            var tempStudentName = "<tr><td><label for='studentFname'>First Name</label></td><td><input  type='text' id='studentFname' placeholder='First Name' class='form-control' name='studentFname'/></td></tr>";
        }else{
            var tempStudentName = "<tr><td><label for='studentFname'>First Name</label></td><td><input  type='text' value="+fname+"  id='studentFname' class='form-control' name='studentFname'/></td></tr>";
        }

        if(lname == ''){
            var tempStudentLastName = "<tr><td><label for='studentLname'>Last Name</label></td><td><input type='text'  id='studentLname' placeholder='Last Name' class='form-control' name='studentLname'/></td></tr>";
        }   else{
            var tempStudentLastName = "<tr><td><label for='studentLname'>Last Name</label></td><td><input type='text' value="+lname+"  id='studentLname' class='form-control' name='studentLname'/></td></tr>";
        }


        var formStudentHTML ='<div class="col-md-offset-4 col-md-8"><div style="width: 40%;" class="panel panel-default"><form id="edit_Student_Form'+id+'" name="edit_Student_Form'+id+'" onsubmit="event.preventDefault();">'+'<div class="panel-body"><table cellpadding="0" width="60%;" cellspacing="0" border="0">'+
            tempStudentId +tempStudentName+ tempStudentLastName+tempStudentUserName+
            '</table></div><div class="panel-body"><div class="btn-toolbar"><button role="button" onclick="updateStudentInfo('+id+')" class="btn btn-primary">Save Changes</button><button role="button" onclick="resetPassWordForThisStudent('+id+',\'' + uname + '\')" type="button" class="btn btn-primary">Reset Password</button></div></div></form></div></div>';
        row.child(formStudentHTML).show();

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
                }
            }
        });

    });

    var higherlevelDetail = "<div id=" + data[0] + " class='panel-body animated zoomOut'> " +
        " <div class='panel panel-default'> <div class='panel-body'><strong>Problem Set: " + JSONData["topicName"] + "</strong></div> " +
        " <div class='panel-body'><strong>Standards: " + html + "</strong></div>" +
        " <div class='panel-body'><strong>Summary : " + JSONData["topicSummary"] + "</strong></div>"+
        "<div class='panel-body'>Students will see the following selected problems for this problem set. Check/Uncheck  problems you wish to add/remove and click button below</div>"+
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

}

function registerAllEvents(){
    $('#wrapper').toggleClass('toggled');
    $('#reorg_prob_sets_handler').css('background-color','#e6296f');
    $('#reorg_prob_sets_handler').css('color', '#ffffff');

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

    inactivetable = $('#inActiveProbSetTable').DataTable({
        "bPaginate": false,
        "bFilter": false,
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
                    $("#successMsgModelPopup").find("[class*='modal-body']").html( "The Selected problemsets are deactivated. Please refresh you page o view your changes" );
                    $('#successMsgModelPopup').modal('show');
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
                    $("#successMsgModelPopup").find("[class*='modal-body']").html( "The Selected problemsets are activated. Please refresh you page to view your changes" );
                    $('#successMsgModelPopup').modal('show');
                }
            }
        });

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

    activetable.on( 'row-reorder', function ( e, diff, edit ) {
        activetable.$('input').removeAttr( 'checked' );
        var result = [];
        console.log(pgContext);
        for ( var i=0; i< diff.length ; i++ ) {
            var rowData = activetable.row( diff[i].node ).data();
            result[i] = rowData[3]+'~~'+ diff[i].newData+'~~'+diff[i].oldData;
        }
        $('#centerSpinner').show();
        $('#reOrderMsg').show();
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

    $('#report_one').click(function() {

    });

    $('#report_two').click(function() {


    });

    $('#report_three').click(function() {
        $.ajax({
            type : "POST",
            url : pgContext+"/tt/tt/getTeacherReports",
            data : {
                classId: classID,
                teacherId: teacherID,
                reportType: 'perStudentReport'
            },
            success : function(data) {
                var jsonData = jsonData = $.parseJSON(data);
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

    $('#report_four').click(function() {

    });

    $('#report_five').click(function() {

    });

    /** Report Handler Ends **/


}