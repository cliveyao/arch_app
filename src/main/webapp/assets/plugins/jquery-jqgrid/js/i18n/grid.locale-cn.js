;(function($){
/**
 * jqGrid Chinese Translation
 * 咖啡兔 yanhonglei@gmail.com
 * http://www.kafeitu.me 
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
**/
$.jgrid = $.jgrid || {};
$.extend($.jgrid,{
    defaults : {
        recordtext: "{0} - {1}\u3000 Altogether {2}Article", 
        emptyrecords: "No data",
        loadtext: "Loading ...",
        pgtext : " {0} 共 {1} page"
    },
    search : {
        caption: "search for...",
        Find: "Find",
        Reset: "Reset",
        odata: [{ oper:'eq', text:'equal \u3000\u3000'},{ oper:'ne', text:'Unequal \u3000\u3000'},{ oper:'lt', text:'Less than \u3000\u3000'},{ oper:'le', text:'Less than or equal'},{ oper:'gt', text:'more than the \u3000\u3000'},{ oper:'ge', text:'greater or equal to '},{ oper:'bw', text:'Starts at'},{ oper:'bn', text:'Not begin to'},{ oper:'in', text:'belong\u3000\u3000'},{ oper:'ni', text:'Does not belong'},{ oper:'ew', text:'Ends'},{ oper:'en', text:'Do not end in'},{ oper:'cn', text:'contain \u3000\u3000'},{ oper:'nc', text:'Does not contain'},{ oper:'nu', text:'does not exist'},{ oper:'nn', text:'exist'}],
        groupOps: [ { op: "AND", text: "all" },    { op: "OR",  text: "Either" } ],
		operandTitle : "Click to select search operation.",
		resetTitle : "Reset Search Value"
    },
    edit : {
        addCaption: "Add Record",
        editCaption: "Edit Records",
        bSubmit: "submit",
        bCancel: "cancel",
        bClose: "Cancel",
        saveData: "Data has been changed , you want to save ?",
        bYes : "Yes",
        bNo : "No",
        bExit : "cancel",
        msg: {
            required:"This field is required",
            number:"Please enter a valid number",
            minValue:"The output value must be greater than or equal ",
            maxValue:"The output value must be less than or equal ",
            email: "This is not a valid e-mail address",
            integer: "Please enter a valid integer",
            date: "Please enter a valid time",
            url: "Invalid URL . Prefix must be ( 'http: //' or 'https: //')",
            nodefined : " Undefined !",
            novalue : " Need to return value !",
            customarray : "Custom function needs to return an array !",
            customfcheck : "There must be a custom function !"
        }
    },
    view : {
        caption: "View Record",
        bClose: "Close"
    },
    del : {
        caption: "delete",
        msg: "Delete the selected records ?",
        bSubmit: "delete",
        bCancel: "cancel"
    },
    nav : {
        edittext: "",
        edittitle: "Edit selected record",
        addtext:"",
        addtitle: "Add a new record",
        deltext: "",
        deltitle: "Delete the selected records",
        searchtext: "",
        searchtitle: "Find",
        refreshtext: "",
        refreshtitle: "Refresh Table",
        alertcap: "note",
        alerttext: "Select Records",
        viewtext: "",
        viewtitle: "View Selected Records"
    },
    col : {
        caption: "Choose Columns",
        bSubmit: "determine",
        bCancel: "cancel"
    },
    errors : {
        errcap : "error",
        nourl : "Not set url",
        norecords: "Records are not to be treated",
        model : "colNames and colModel different lengths !"
    },
    formatter : {
        integer : {thousandsSeparator: ",", defaultValue: '0'},
        number : {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'},
        currency : {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "", suffix:"", defaultValue: '0.00'},
        date : {
            dayNames:   [
                "Day" , "one" , " two", " three" , "four" , "five ", " six" ,
                "Sunday ", " Monday" , " Tuesday" , " Wednesday ", " Thursday" , "Friday" , " Saturday",
            ],
            monthNames: [
                         "One" , " two", " three" , "four" , "five ", " six" , "seven" , "eight" , "Nine" , "ten" , "eleven" , " twelve" ,
                " January " , "February " , "March " , "April ", " May ", " June ", " July" , " August ", " September ", "October " " November ", " December "
            ],
            AmPm : ["am","pm", "morning", "afternoon"],
            S: function (j) {return j < 11 || j > 13 ? ['st', 'nd', 'rd', 'th'][Math.min((j - 1) % 10, 3)] : 'th';},
            srcformat: 'Y-m-d',
            newformat: 'Y-m-d',
            parseRe : /[#%\\\/:_;.,\t\s-]/,
            masks : {
                // see http://php.net/manual/en/function.date.php for PHP format used in jqGrid
                // and see http://docs.jquery.com/UI/Datepicker/formatDate
                // and https://github.com/jquery/globalize#dates for alternative formats used frequently
                // one can find on https://github.com/jquery/globalize/tree/master/lib/cultures many
                // information about date, time, numbers and currency formats used in different countries
                // one should just convert the information in PHP format
                ISO8601Long:"Y-m-d H:i:s",
                ISO8601Short:"Y-m-d",
                // short date:
                //    n - Numeric representation of a month, without leading zeros
                //    j - Day of the month without leading zeros
                //    Y - A full numeric representation of a year, 4 digits
                // example: 3/1/2012 which means 1 March 2012
                ShortDate: "n/j/Y", // in jQuery UI Datepicker: "M/d/yyyy"
                // long date:
                //    l - A full textual representation of the day of the week
                //    F - A full textual representation of a month
                //    d - Day of the month, 2 digits with leading zeros
                //    Y - A full numeric representation of a year, 4 digits
                LongDate: "l, F d, Y", // in jQuery UI Datepicker: "dddd, MMMM dd, yyyy"
                // long date with long time:
                //    l - A full textual representation of the day of the week
                //    F - A full textual representation of a month
                //    d - Day of the month, 2 digits with leading zeros
                //    Y - A full numeric representation of a year, 4 digits
                //    g - 12-hour format of an hour without leading zeros
                //    i - Minutes with leading zeros
                //    s - Seconds, with leading zeros
                //    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
                FullDateTime: "l, F d, Y g:i:s A", // in jQuery UI Datepicker: "dddd, MMMM dd, yyyy h:mm:ss tt"
                // month day:
                //    F - A full textual representation of a month
                //    d - Day of the month, 2 digits with leading zeros
                MonthDay: "F d", // in jQuery UI Datepicker: "MMMM dd"
                // short time (without seconds)
                //    g - 12-hour format of an hour without leading zeros
                //    i - Minutes with leading zeros
                //    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
                ShortTime: "g:i A", // in jQuery UI Datepicker: "h:mm tt"
                // long time (with seconds)
                //    g - 12-hour format of an hour without leading zeros
                //    i - Minutes with leading zeros
                //    s - Seconds, with leading zeros
                //    A - Uppercase Ante meridiem and Post meridiem (AM or PM)
                LongTime: "g:i:s A", // in jQuery UI Datepicker: "h:mm:ss tt"
                SortableDateTime: "Y-m-d\\TH:i:s",
                UniversalSortableDateTime: "Y-m-d H:i:sO",
                // month with year
                //    Y - A full numeric representation of a year, 4 digits
                //    F - A full textual representation of a month
                YearMonth: "F, Y" // in jQuery UI Datepicker: "MMMM, yyyy"
            },
            reformatAfterEdit : false
        },
        baseLinkUrl: '',
        showAction: '',
        target: '',
        checkbox : {disabled:true},
        idName : 'id'
    }
});
})(jQuery);
