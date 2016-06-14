jQuery(document).ready(
		function() {
			$("#login-form").find("input[name='username']").focus();
			$("#login-form").validate({
				errorElement : "span",
				errorClass : "help-block",
				focusInvalid : false,
				rules : {
					username : {
						required : true
					},
					password : {
						required : true
					},
					captcha : {
						required : true
					}
				},
				messages : {
					username : {
						required : "Please fill in the login account"
					},
					password : {
						required : "Please fill in the login password"
					},
					captcha : {
						required : "Please fill in the login codes"
					}
				},
				highlight : function(b) {
					$(b).closest(".form-group").addClass("has-error")
				},
				success : function(b) {
					b.closest(".form-group").removeClass("has-error");
					b.remove()
				},
				errorPlacement : function(b, c) {
					b.appendTo(c.closest(".form-group"))
				},
				submitHandler : function(b) {
					b.submit()
				}
			});
			var a = "h";
			if ($(window).height() > $(window).width()) {
				a = "v"
			}
			$.backstretch([ WEB_ROOT + "/assets/img/bg01_" + a + ".jpg",
					WEB_ROOT + "/assets/img/bg02_" + a + ".jpg" ], {
				fade : 1000,
				duration : 8000
			});
			$("#reset-form").validate({
				errorElement : "span",
				errorClass : "help-block",
				focusInvalid : false,
				ignore : "",
				rules : {
					password : {
						required : true
					},
					rpassword : {
						required : true,
						equalToByName : "password"
					}
				},
				messages : {
					password : {
						required : "Please enter your password"
					},
					rpassword : {
						required : "Please enter your login password again",
						equalTo : "Confirm Password must match Password"
					}
				},
				highlight : function(b) {
					$(b).closest(".form-group").addClass("has-error")
				},
				success : function(b) {
					b.closest(".form-group").removeClass("has-error");
					b.remove()
				},
				errorPlacement : function(b, c) {
					b.insertAfter(c)
				},
				submitHandler : function(b) {
					$(b).ajaxPostForm(function() {
						$(b).find('button[data-dismiss="modal"]').click();
						bootbox.dialog({
							message : "You can now use the new system it set the password",
							title : "Congratulations , password setting success",
							buttons : {
								main : {
									label : "shut down",
									className : "blue"
								}
							}
						})
					});
					return false
				}
			})
		});