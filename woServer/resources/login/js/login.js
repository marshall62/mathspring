/**
 * Created by david on 8/10/2016.
 */

function validateForm(isMultipleChoiceQuest) {
    var v;
    console.log("validateForm"+ isMultipleChoiceQuest);
    if (isMultipleChoiceQuest) {
        a = document.getElementById("a")!=null && document.getElementById("a").checked == true;
        b = document.getElementById("b")!=null && document.getElementById("b").checked == true;
        c = document.getElementById("c")!=null && document.getElementById("c").checked == true;
        d = document.getElementById("d")!=null && document.getElementById("d").checked == true;
        e = document.getElementById("e")!=null && document.getElementById("e").checked == true;
        v = a || b || c || d || e;

        if (!v) {
            alert("Please select one of the answers!");
        }
    }
    else {
        v = true;
        var input = document.getElementById("f").value.trim();
        if(input == "")
        {
            alert('Please answer the question!');
            document.getElementById("f").focus();
            v = false
        }
    }
    return v;
}
