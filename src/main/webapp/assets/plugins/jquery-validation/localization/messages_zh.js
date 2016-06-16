/*
 * Translated default messages for the jQuery validation plugin.
 * Locale: ZH (Chinese, 中文 (Zhōngwén), 汉语, 漢語)
 */
(function ($) {
	$.extend($.validator.messages, {
		required: "Required field",
		remote: "Please fix this field",
		email: "Please enter a properly formatted e-mail",
		url: "Please enter a valid URL",
		date: "Please enter a valid date",
		dateISO: "Please enter a valid date (ISO).",
		number: "Please enter a valid number",
		digits: "Enter only integer",
		creditcard: "Please enter a valid credit card number",
		equalTo: "Please enter the same value again",
		accept: "Please enter the string has a legitimate extension",
		maxlength: $.validator.format("Please enter a maximum length of the string is { 0 }"),
		minlength: $.validator.format("Please enter a minimum length of the string is { 0 }"),
		rangelength: $.validator.format("Please enter a length between { 0 } and { 1} string "),
		range: $.validator.format("Please enter a value between { 0 } and { 1}"),
		max: $.validator.format("Please enter a maximum value of { 0 }"),
		min: $.validator.format("Please enter a minimum value of {0 }")
	});
}(jQuery));