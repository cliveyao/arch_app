var FormValidation = function() {
	return {
		init : function() {
			$.fn.setFormDatas = function(data, forceOverwrite) {
				var $container = $(this);
				for ( var key in data) {
					var el = "input[name='" + key + "'],select[name='" + key
							+ "'],textarea[name='" + key + "']";
					var $el = $container.find(el);
					if (forceOverwrite == false) {
						if ($.trim($el.val()) != "") {
							continue
						}
					}
					var val = data[key];
					$el.val(val);
					if ($el.is("select")) {
						$el.select2()
					}
				}
			}, jQuery.validator.addMethod("unique", function(value, element) {
				var form = $(element).closest("form");
				var rules = form.attr("data-editrulesurl");
				Util.assertNotBlank(rules, "Form data-editrulesurl property is undefined");
				var url = WEB_ROOT + "/admin/util/validate/unique?clazz="
						+ rules.split("clazz=")[1] + "&element="
						+ $(element).attr("name");
				var id = form.find("input[name='id']");
				if (id.size() > 0) {
					url = url + "&id=" + id.val()
				}
				return $.validator.methods.remote.call(this, value, element,
						url)
			}, "Data already exists");
			jQuery.validator
					.addMethod(
							"timestamp",
							function(value, element) {
								if (value == "") {
									return this.optional(element)
								}
								var regex = /^(?:[0-9]{4})-(?:(?:0[1-9])|(?:1[0-2]))-(?:(?:[0-2][1-9])|(?:[1-3][0-1])) (?:(?:[0-2][0-3])|(?:[0-1][0-9])):[0-5][0-9]:[0-5][0-9]$/;
								if (!regex.test(value)) {
									return false
								}
								return true
							}, "Please enter a valid date and time format ( such as 2011-08-15 13:40:00）");
			jQuery.validator
					.addMethod(
							"shortTimestamp",
							function(value, element) {
								if (value == "") {
									return this.optional(element)
								}
								var regex = /^(?:[0-9]{4})-(?:(?:0[1-9])|(?:1[0-2]))-(?:(?:[0-2][1-9])|(?:[1-3][0-1])) (?:(?:[0-2][0-3])|(?:[0-1][0-9])):[0-5][0-9]$/;
								if (!regex.test(value)) {
									return false
								}
								return true
							}, "Please enter a valid date and time format ( such as 2011-08-15 13:40）");
			jQuery.validator.addMethod("yearMonth", function(value, element) {
				if (value == "") {
					return this.optional(element)
				}
				var regex = /^(?:[0-9]{4})(?:(?:0[1-9])|(?:1[0-2]))$/;
				if (!regex.test(value)) {
					return false
				}
				return true
			}, "Please enter a valid date format ( such as 201201）");
			jQuery.validator.addMethod("startWith", function(value, element,
					param) {
				if (this.optional(element)) {
					return true
				}
				if (param.length > value.length) {
					return false
				}
				if (value.substr(0, param.length) == param) {
					return true
				} else {
					return false
				}
			}, "Please enter the beginning of the string { 0}");
			jQuery.validator.addMethod("dateLT",
					function(value, element, param) {
						if (value == "") {
							return this.optional(element)
						}
						var $param = $(param);
						if ($param.size() == 0) {
							$param = $(element).closest("form").find(
									"[name='" + param + "']")
						}
						var endDate = $param.val();
						if (endDate == "") {
							return true
						}
						var startDate = eval("new Date("
								+ value.replace(/[\-\s:]/g, ",") + ")");
						endDate = eval("new Date("
								+ endDate.replace(/[\-\s:]/g, ",") + ")");
						if (startDate > endDate) {
							return false
						} else {
							return true
						}
					}, "Date input data must be less than the end date");
			jQuery.validator.addMethod("dateGT",
					function(value, element, param) {
						if (value == "") {
							return this.optional(element)
						}
						var $param = $(param);
						if ($param.size() == 0) {
							$param = $(element).closest("form").find(
									"[name='" + param + "']")
						}
						var startDate = $param.val();
						if (startDate == "") {
							return true
						}
						var endDate = eval("new Date("
								+ value.replace(/[\-\s:]/g, ",") + ")");
						startDate = eval("new Date("
								+ startDate.replace(/[\-\s:]/g, ",") + ")");
						if (startDate > endDate) {
							return false
						} else {
							return true
						}
					}, "Date input data must be greater than start date");
			var idCardNoUtil = {
				provinceAndCitys : {
				    	11 : " Beijing ",
						12: " Tianjin ",
						13: " Hebei ",
						14: " Shanxi ",
						15: " Inner Mongolia ",
						21: " Liaoning ",
						22: " Jilin ",
						23: " Heilongjiang ",
						31: " Shanghai ",
						32: "Jiangsu ",
						33: "Zhejiang ",
						34: " Anhui ",
						35: " Fujian ",
						36: " Jiangxi ",
						37: "Shandong ",
						41: " Henan ",
						42: "Hubei ",
						43: " Hunan ",
						44: "Guangdong ",
						45: " Guangxi ",
						46: "Hainan ",
						50: " Chongqing ",
						51: " Sichuan ",
						52: "Guizhou ",
						53: " Yunnan ",
						54: "Tibet ",
						61: " Shaanxi ",
						62: " Gansu ",
						63: " Qinghai ",
						64: " Ningxia ",
						65: " Xinjiang ",
						71: " Taiwan ",
						81: "Hong Kong" ,
						82 : " Macao ",
						99 : "Other "
				},
				powers : [ "7", "9", "10", "5", "8", "4", "2", "1", "6", "3",
						"7", "9", "10", "5", "8", "4", "2" ],
				parityBit : [ "1", "0", "X", "9", "8", "7", "6", "5", "4", "3",
						"2" ],
				genders : {
					male : "male",
					female : "female"
				},
				checkAddressCode : function(addressCode) {
					var check = /^[1-9]\d{5}$/.test(addressCode);
					if (!check) {
						return false
					}
					if (idCardNoUtil.provinceAndCitys[parseInt(addressCode
							.substring(0, 2))]) {
						return true
					} else {
						return false
					}
				},
				checkBirthDayCode : function(birDayCode) {
					var check = /^[1-9]\d{3}((0[1-9])|(1[0-2]))((0[1-9])|([1-2][0-9])|(3[0-1]))$/
							.test(birDayCode);
					if (!check) {
						return false
					}
					var yyyy = parseInt(birDayCode.substring(0, 4), 10);
					var mm = parseInt(birDayCode.substring(4, 6), 10);
					var dd = parseInt(birDayCode.substring(6), 10);
					var xdata = new Date(yyyy, mm - 1, dd);
					if (xdata > new Date()) {
						return false
					} else {
						if ((xdata.getFullYear() == yyyy)
								&& (xdata.getMonth() == mm - 1)
								&& (xdata.getDate() == dd)) {
							return true
						} else {
							return false
						}
					}
				},
				getParityBit : function(idCardNo) {
					var id17 = idCardNo.substring(0, 17);
					var power = 0;
					for (var i = 0; i < 17; i++) {
						power += parseInt(id17.charAt(i), 10)
								* parseInt(idCardNoUtil.powers[i])
					}
					var mod = power % 11;
					return idCardNoUtil.parityBit[mod]
				},
				checkParityBit : function(idCardNo) {
					var parityBit = idCardNo.charAt(17).toUpperCase();
					if (idCardNoUtil.getParityBit(idCardNo) == parityBit) {
						return true
					} else {
						return false
					}
				},
				checkIdCardNo : function(idCardNo) {
					if (Util.startWith(idCardNo, "99")) {
						return true
					}
					var check = /^\d{15}|(\d{17}(\d|x|X))$/.test(idCardNo);
					if (!check) {
						return false
					}
					if (idCardNo.length == 15) {
						return idCardNoUtil.check15IdCardNo(idCardNo)
					} else {
						if (idCardNo.length == 18) {
							return idCardNoUtil.check18IdCardNo(idCardNo)
						} else {
							return false
						}
					}
				},
				check15IdCardNo : function(idCardNo) {
					var check = /^[1-9]\d{7}((0[1-9])|(1[0-2]))((0[1-9])|([1-2][0-9])|(3[0-1]))\d{3}$/
							.test(idCardNo);
					if (!check) {
						return false
					}
					var addressCode = idCardNo.substring(0, 6);
					check = idCardNoUtil.checkAddressCode(addressCode);
					if (!check) {
						return false
					}
					var birDayCode = "19" + idCardNo.substring(6, 12);
					return idCardNoUtil.checkBirthDayCode(birDayCode)
				},
				check18IdCardNo : function(idCardNo) {
					var check = /^[1-9]\d{5}[1-9]\d{3}((0[1-9])|(1[0-2]))((0[1-9])|([1-2][0-9])|(3[0-1]))\d{3}(\d|x|X)$/
							.test(idCardNo);
					if (!check) {
						return false
					}
					var addressCode = idCardNo.substring(0, 6);
					check = idCardNoUtil.checkAddressCode(addressCode);
					if (!check) {
						return false
					}
					var birDayCode = idCardNo.substring(6, 14);
					check = idCardNoUtil.checkBirthDayCode(birDayCode);
					if (!check) {
						return false
					}
					return check
				},
				formateDateCN : function(day) {
					if (idCardNoUtil.checkBirthDayCode(day)) {
						var yyyy = day.substring(0, 4);
						var mm = day.substring(4, 6);
						var dd = day.substring(6);
						return yyyy + "-" + mm + "-" + dd
					}
					return ""
				},
				getIdCardInfo : function(idCardNo) {
					var idCardInfo = {
						gender : "",
						birthday : ""
					};
					if (idCardNo.length == 15) {
						var aday = "19" + idCardNo.substring(6, 12);
						idCardInfo.birthday = idCardNoUtil.formateDateCN(aday);
						if (parseInt(idCardNo.charAt(14)) % 2 == 0) {
							idCardInfo.gender = idCardNoUtil.genders.female
						} else {
							idCardInfo.gender = idCardNoUtil.genders.male
						}
					} else {
						if (idCardNo.length == 18) {
							var aday = idCardNo.substring(6, 14);
							idCardInfo.birthday = idCardNoUtil
									.formateDateCN(aday);
							if (parseInt(idCardNo.charAt(16)) % 2 == 0) {
								idCardInfo.gender = idCardNoUtil.genders.female
							} else {
								idCardInfo.gender = idCardNoUtil.genders.male
							}
						}
					}
					return idCardInfo
				},
				getId15 : function(idCardNo) {
					if (idCardNo.length == 15) {
						return idCardNo
					} else {
						if (idCardNo.length == 18) {
							return idCardNo.substring(0, 6)
									+ idCardNo.substring(8, 17)
						} else {
							return null
						}
					}
				},
				getId18 : function(idCardNo) {
					if (idCardNo.length == 15) {
						var id17 = idCardNo.substring(0, 6) + "19"
								+ idCardNo.substring(6);
						var parityBit = idCardNoUtil.getParityBit(id17);
						return id17 + parityBit
					} else {
						if (idCardNo.length == 18) {
							return idCardNo
						} else {
							return null
						}
					}
				}
			};
			jQuery.validator.addMethod("idCardNo", function(value, element,
					param) {
				return this.optional(element)
						|| idCardNoUtil.checkIdCardNo(value)
			}, "Please enter a valid ID number");
			jQuery.validator.addMethod("phone", function(value, element) {
				var phone = /^\d|-$/;
				return this.optional(element) || (phone.test(value))
			}, "Please enter a valid phone numbers : numbers or '-'");
			jQuery.validator.addMethod("mobile", function(value, element) {
				var phone = /^1\d{10}$/;
				return this.optional(element) || (phone.test(value))
			}, "Please enter a valid phone number");
			jQuery.validator.addMethod("zipCode", function(value, element) {
				var tel = /^[0-9]{6}$/;
				return this.optional(element) || (tel.test(value))
			}, "Please enter a valid six-digit postal code");
			jQuery.validator.addMethod("numberEndWithPointFive", function(
					value, element) {
				var reg = /^(0|[1-9]\d*)([.][05])?$/;
				return this.optional(element) || (reg.test(value))
			}, ".0 Or .5 must end as a decimal");
			jQuery.validator.addMethod("equalToByName", function(value,
					element, param) {
				var target = $(element).closest("form").find(
						"input[name='" + param + "']");
				if (this.settings.onfocusout) {
					target.unbind(".validate-equalTo").bind(
							"blur.validate-equalTo", function() {
								$(element).valid()
							})
				}
				return value === target.val()
			}, "Please enter the data before and after the equal");
			jQuery.validator.addMethod("idCardNoSex", function(value, element,
					param) {
				var target = $(element).closest("form").find(
						"input[name='" + param + "']:checked");
				var seven = value.substring(16, 17);
				return this.optional(element)
						|| (seven % 2 === target.val() % 2)
			}, "ID number and gender do not match");
			jQuery.validator.addMethod("requiredByName", function(value,
					element, param) {
				var target = $(element).closest("form").find(
						"input[name='" + param + "']");
				if (target.val().length > 0) {
					if (value == undefined || value.length == 0) {
						return false
					}
				}
				return true
			}, "Because this data dependency must be filled");
			jQuery.validator
					.addMethod(
							"remoteCallback",
							function(value, element, param) {
								if (this.optional(element)) {
									return "dependency-mismatch"
								}
								var previous = this.previousValue(element);
								if (!this.settings.messages[element.name]) {
									this.settings.messages[element.name] = {}
								}
								previous.originalMessage = this.settings.messages[element.name].remote;
								this.settings.messages[element.name].remote = previous.message;
								param = typeof param === "string" && {
									url : param
								} || param;
								if (previous.old === value) {
									return previous.valid
								}
								previous.old = value;
								var validator = this;
								this.startRequest(element);
								var data = {};
								data[element.name] = value;
								$
										.ajax($
												.extend(
														true,
														{
															url : param,
															mode : "abort",
															port : "validate"
																	+ element.name,
															dataType : "json",
															data : data,
															success : function(
																	response) {
																validator.settings.messages[element.name].remote = previous.originalMessage;
																var valid = response === true
																		|| response === "true";
																if (valid) {
																	var submitted = validator.formSubmitted;
																	validator
																			.prepareElement(element);
																	validator.formSubmitted = submitted;
																	validator.successList
																			.push(element);
																	delete validator.invalid[element.name];
																	validator
																			.showErrors()
																} else {
																	var showError = true;
																	var callback = $(
																			element)
																			.attr(
																					"data-remoteCallback");
																	if (callback) {
																		var result = eval(callback);
																		if (result != undefined
																				&& result === false) {
																			showError = false
																		}
																	}
																	if (showError) {
																		var errors = {};
																		var message = response
																				|| validator
																						.defaultMessage(
																								element,
																								"remoteCallback");
																		errors[element.name] = previous.message = $
																				.isFunction(message) ? message(value)
																				: message;
																		validator.invalid[element.name] = true;
																		validator
																				.showErrors(errors)
																	}
																}
																previous.valid = valid;
																validator
																		.stopRequest(
																				element,
																				valid)
															}
														}, param));
								return "pending"
							}, "Data validation is not over");
			$.validator.addMethod("regex", function(value, element, regexp) {
				var re = new RegExp(regexp);
				return this.optional(element) || re.test(value)
			}, "Data validation does not pass");
			$.validator.addMethod("custom", function(value, element, param) {
				var custom = $(element).attr("data-rule-custom");
				if (custom == "true") {
					$(element).attr("data-rule-custom", "false");
					return false
				} else {
					return true
				}
			}, "Custom validation does not pass")
		},
		initAjax : function($container) {
			if ($container == undefined) {
				$container = $("body")
			}
			$("form.form-validation", $container)
					.each(
							function() {
								var $form = $(this);
								var dataGridSearch = $form
										.attr("data-grid-search");
								var dataDivSearch = $form
										.attr("data-div-search");
								$form
										.find('[type="reset"]')
										.click(
												function() {
													if (dataGridSearch) {
														$(dataGridSearch)
																.each(
																		function() {
																			$(this)[0]
																					.clearToolbar(false)
																		})
													}
													setTimeout(
															function() {
																$form
																		.find(
																				'[data-toggle="buttons"] label.btn > input.toggle')
																		.each(
																				function() {
																					if ($(
																							this)
																							.is(
																									":checked")) {
																						$(
																								this)
																								.parent()
																								.addClass(
																										"active")
																					} else {
																						$(
																								this)
																								.parent()
																								.removeClass(
																										"active")
																					}
																				});
																$form
																		.find(
																				"input[data-date-init]")
																		.each(
																				function() {
																					var $datapicker = $(this);
																					var dataPicker = $datapicker
																							.attr("data-picker");
																					var initVal = $datapicker
																							.attr("data-date-init");
																					if (initVal
																							&& "today" == initVal) {
																						var today = moment()
																								.format(
																										"YYYY-MM-DD");
																						if (dataPicker == "date-range") {
																							$datapicker
																									.val(today
																											+ " ~ "
																											+ today)
																						} else {
																							$datapicker
																									.val(today)
																						}
																					}
																				});
																$form
																		.find(
																				"select")
																		.each(
																				function() {
																					$(
																							this)
																							.select2(
																									"val",
																									$(
																											this)
																											.val())
																				});
																if (dataGridSearch
																		|| dataDivSearch) {
																	$form
																			.submit()
																}
															}, 100);
													return
												});
								var options = $.extend(true, {
									editrulesurl : $form
											.attr("data-editrulesurl")
								}, $form.data("formOptions") || {});
								if (options.editrulesurl == undefined
										&& $form.attr("method").toUpperCase() == "POST") {
									var action = $form.attr("action")
								}
								var $els = $form
										.find(":input[type='text'], :input[type='password'], :input[type='radio'], :input[type='checkbox'], :input[type='file'],:input[type='hidden'], select , textarea");
								$els
										.each(function() {
											var $el = $(this);
											if ($el.attr("required")) {
												var $formGroup = $el
														.closest(".form-group");
												if ($formGroup
														.find("label.control-label > span.required").length == 0) {
													$formGroup
															.find(
																	"label.control-label")
															.append(
																	'<span class="required">*</span>')
												}
											}
										});
								if ($form.attr("method") == "post"
										&& !$form
												.hasClass("form-track-disabled")) {
									$form.on("change", $els, function() {
										$form
												.attr("form-data-modified",
														"true")
									});
									$form.on("keyup", "textarea", function() {
										$form
												.attr("form-data-modified",
														"true")
									})
								}
								$form
										.find(":text")
										.each(
												function() {
													var $el = $(this);
													$el.blur(function() {
														$el.val($.trim($el
																.val()))
													});
													var spellTo = $el
															.attr("data-spell-to");
													var Pinyin = true;
													if (Pinyin && spellTo) {
														$el
																.change(function() {
																	var val = $el
																			.val();
																	if (val != "") {
																		var $spellTo = $form
																				.find('input[name="'
																						+ spellTo
																						+ '"]');
																		$spellTo
																				.val(Pinyin
																						.getCamelChars(val))
																	}
																})
													}
												});
								var validator = $form
										.validate({
											errorElement : "span",
											errorClass : "error-block",
											focusInvalid : true,
											ignore : ":disabled",
											errorPlacement : function(error,
													element) {
												var $errorPlacement = element
														.closest(".form-group")
														.find(
																".error-placement");
												if ($errorPlacement.size() > 0) {
													error
															.appendTo($errorPlacement)
												} else {
													if (element.parent(
															".input-group")
															.size() > 0) {
														error
																.insertAfter(element
																		.parent(".input-group"))
													} else {
														if (element
																.parent(
																		".input-icon")
																.parent(
																		".input-group")
																.size()) {
															error
																	.insertAfter(element
																			.parent(
																					".input-icon")
																			.parent(
																					".input-group"))
														} else {
															if (element
																	.attr("data-error-container")) {
																error
																		.appendTo($(element
																				.attr("data-error-container")))
															} else {
																if (element
																		.parents(
																				".controls-radiobuttons")
																		.size() > 0) {
																	error
																			.appendTo(element
																					.parents(".controls-radiobuttons"))
																} else {
																	if (element
																			.parents(
																					".radio-list")
																			.size() > 0) {
																		error
																				.insertAfter(element
																						.parents(".radio-list"))
																	} else {
																		if (element
																				.parent(
																						".radio-inline")
																				.size() > 0) {
																			error
																					.appendTo(element
																							.parent(
																									".radio-inline")
																							.parent())
																		} else {
																			if (element
																					.parents(
																							".controls-checkboxes")
																					.size() > 0) {
																				error
																						.appendTo(element
																								.parents(".controls-checkboxes"))
																			} else {
																				if (element
																						.parents(
																								".checkbox-list")
																						.size() > 0) {
																					error
																							.insertAfter(element
																									.parents(".checkbox-list"))
																				} else {
																					if (element
																							.parent(
																									".checkbox-inline")
																							.size() > 0) {
																						error
																								.appendTo(element
																										.parent(
																												".checkbox-inline")
																										.parent())
																					} else {
																						if (element
																								.parent(
																										".check-radio-label")
																								.size() > 0) {
																							error
																									.appendTo(element
																											.parent(
																													".check-radio-label")
																											.parent())
																						} else {
																							error
																									.insertAfter(element)
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											},
											invalidHandler : function(event,
													validator) {
												App
														.scrollTo(
																$(
																		validator.errorList[0].element)
																		.closest(
																				".form-group"),
																-100)
											},
											highlight : function(element) {
												$(element).closest(
														".form-group")
														.addClass("has-error");
												$(element).closest("td")
														.addClass("has-error")
											},
											unhighlight : function(element) {
												var $errorPlacement = $(element)
														.closest(".form-group")
														.find(
																".error-placement");
												if ($errorPlacement.size() > 0) {
													if ($errorPlacement.find(
															".error-block")
															.size() > 0) {
														return
													}
												}
												$(element).closest("td")
														.removeClass(
																"has-error");
												$(element).closest(
														".form-group")
														.removeClass(
																"has-error");
												$(element)
														.closest("form")
														.find(
																".form-error-placement")
														.empty()
											},
											success : function(label) {
												label.remove()
											},
											submitHandler : function(form) {
												var validator = this;
												var submitButton = $(this.submitButton);
												if (submitButton
														.attr("data-form-action")) {
													$form
															.attr(
																	"action",
																	submitButton
																			.attr("data-form-action"))
												}
												if (submitButton
														.attr("data-confirm")) {
													if (!confirm(submitButton
															.attr("data-confirm"))) {
														return false
													}
												}
												if (options.submitHandler) {
													options.submitHandler.call(
															this, form);
													return false
												}
												var ret = $form
														.trigger("form-validate-success");
												if (ret != undefined
														&& ret == false) {
													return
												}
												var target = $form
														.attr("target");
												if (target) {
													form.submit();
													return true
												}
												if (dataGridSearch) {
													var $grid = $(dataGridSearch);
													var url = $grid
															.data("gridOptions").url;
													if (url.indexOf("?") > -1) {
														url = url
																+ "&"
																+ $form
																		.serialize()
													} else {
														url = url
																+ "?"
																+ $form
																		.serialize()
													}
													var data = {
														datatype : "json",
														url : url
													};
													var bindSearchFormData = {};
													$form
															.find(
																	".grid-param-data")
															.each(
																	function() {
																		var $el = $(this);
																		bindSearchFormData[$el
																				.attr("name")] = $el
																				.val()
																	});
													data.bindSearchFormData = bindSearchFormData;
													if (!$grid
															.hasClass("ui-jqgrid-btable")) {
														Grid.initGrid($grid,
																data)
													} else {
														$grid.jqGrid(
																"setGridParam",
																data).trigger(
																"reloadGrid")
													}
													return
												}
												if (dataDivSearch) {
													var $container = $form
															.closest(".panel-content");
													if ($container.size() == 0) {
														$container = $("body")
													}
													var $target = $container
															.find(dataDivSearch);
													if ($target.is("tbody")) {
														$target
																.parent(
																		"table.table-infinite-scroll")
																.removeAttr(
																		"data-scroll-loading")
																.removeAttr(
																		"data-scroll-page")
													}
													var url = $form
															.attr("action");
													if (url.indexOf("?") > -1) {
														url = url
																+ "&"
																+ $form
																		.serialize()
													} else {
														url = url
																+ "?"
																+ $form
																		.serialize()
													}
													$target.ajaxGetUrl(url,
															false);
													return false
												}
												App.blockUI($form);
												var submitButton = $(this.submitButton);
												submitButton.attr("disabled",
														true);
												var postData = {};
												$('table[data-grid="items"]',
														$form)
														.each(
																function() {
																	var $grid = $(this);
																	postData = $
																			.extend(
																					true,
																					postData,
																					$grid
																							.jqGrid("getDirtyRowdatas"))
																});
												if (submitButton
														.attr("_serverValidationConfirmed_")) {
													postData._serverValidationConfirmed_ = true;
													submitButton
															.removeAttr("_serverValidationConfirmed_")
												}
												var $actions = submitButton
														.closest(".form-actions");
												if ($actions.size() == 0) {
													$actions = submitButton
												}
												$form.find(".alert-validation")
														.remove();
												if (Global.isIELowerVersion()) {
													$form
															.find(
																	"input[type='file']")
															.attr("disabled",
																	true)
												}
												$form
														.ajaxSubmit({
															dataType : "json",
															method : "post",
															data : postData,
															success : function(
																	response) {
																if (Global
																		.isIELowerVersion()) {
																	$form
																			.find(
																					"input[type='file']")
																			.attr(
																					"disabled",
																					false)
																}
																submitButton
																		.attr(
																				"disabled",
																				false);
																App
																		.unblockUI($form);
																$form
																		.find(
																				"input[name='token']")
																		.val(
																				new Date()
																						.getTime());
																if (response.type == "confirm") {
																	bootbox
																			.dialog({
																				closeButton : false,
																				message : response.data ? response.data
																						.join("<br/>")
																						: response.message,
																				title : response.message
																						+ " Make sure to continue to submit the form ?",
																				buttons : {
																					danger : {
																						label : "cancel",
																						callback : function() {
																							submitButton
																									.removeAttr("_serverValidationConfirmed_")
																						}
																					},
																					main : {
																						label : "confirm",
																						className : "blue",
																						callback : function() {
																							submitButton
																									.attr(
																											"_serverValidationConfirmed_",
																											true);
																							submitButton
																									.click()
																						}
																					}
																				}
																			});
																	return
																}
																if (response.type == "success"
																		|| response.type == "warning") {
																	var result = $form
																			.triggerHandler(
																					"form-submit-success",
																					[ response ]);
																	if (result != undefined
																			&& result == false) {
																		return
																	}
																	if (response.message) {
																		Global
																				.notify(
																						response.type,
																						response.message)
																	}
																	$form
																			.attr(
																					"form-data-modified",
																					"false");
																	if (response.redirect) {
																		window.location.href = WEB_ROOT
																				+ response.redirect
																	} else {
																		if (typeof (AdminPage) == "undefined") {
																			var $alert = $form
																					.find("div.alert-edit-success");
																			if ($alert
																					.size() == 0) {
																				$alert = $("<div class='alert alert-success alert-edit-success'>"
																						+ response.message
																						+ "</div>");
																				var $formActions = submitButton
																						.closest(".form-actions");
																				if ($formActions
																						.size() > 0) {
																					$alert
																							.insertBefore($formActions)
																				} else {
																					$alert
																							.prependTo($form)
																				}
																			} else {
																				$alert
																						.html(response.message)
																			}
																		}
																	}
																	var dataGridReload = submitButton
																			.attr("data-grid-reload");
																	var dismiss = submitButton
																			.attr("data-post-dismiss");
																	if (dismiss) {
																		var $modal = submitButton
																				.closest(".modal-dialog");
																		if (dismiss == "modal"
																				&& $modal
																						.size() > 0) {
																			$modal
																					.find(
																							".modal-header button[data-dismiss='modal']")
																					.click()
																		}
																	} else {
																		var reload = submitButton
																				.attr("data-success-reload");
																		if (reload != "false") {
																			var $modal = submitButton
																					.closest(".modal");
																			if ($modal
																					.size() > 0) {
																				var $tabbable = submitButton
																						.closest(
																								".tabbable",
																								$modal);
																				if ($tabbable
																						.size() == 0) {
																					$modal
																							.modal("hide")
																				} else {
																					var $navItems = $tabbable
																							.first()
																							.find(
																									"ul.nav > li:not(.tools)");
																					if ($navItems
																							.size() <= 1) {
																						$modal
																								.modal("hide")
																					} else {
																						var id = submitButton
																								.closest(
																										"form")
																								.find(
																										"input[name='id']")
																								.val();
																						if (id
																								&& id != "") {
																							$tabbable
																									.find(
																											" > ul.nav > li.tools > .reload")
																									.click()
																						} else {
																							var $tabbableContainer = $tabbable
																									.closest(".ajax-get-container");
																							var url = Util
																									.AddOrReplaceUrlParameter(
																											$tabbableContainer
																													.attr("data-url"),
																											"id",
																											response.data.id);
																							$tabbableContainer
																									.ajaxGetUrl(url)
																						}
																					}
																				}
																			}
																			if (reload == "_panel") {
																				var $panelContent = submitButton
																						.closest(".panel-content");
																				$panelContent
																						.ajaxGetUrl($panelContent
																								.attr("data-url"))
																			}
																		}
																	}
																	if (dataGridReload) {
																		$(
																				dataGridReload)
																				.jqGrid(
																						"setGridParam",
																						{
																							datatype : "json"
																						})
																				.trigger(
																						"reloadGrid")
																	}
																} else {
																	if (response.type == "failure"
																			|| response.type == "error") {
																		var ret = $form
																				.triggerHandler(
																						"form-submit-failure",
																						[ response ]);
																		if (ret == undefined
																				|| ret == true) {
																			var $formErrorPlacement = $form
																					.find(".form-error-placement");
																			if ($formErrorPlacement
																					.size() > 0) {
																				$formErrorPlacement
																						.closest(
																								".form-group")
																						.addClass(
																								"has-error");
																				$formErrorPlacement
																						.html('<span class="error-block">'
																								+ response.message
																								+ "</span>")
																			} else {
																				$actions
																						.before("<div class='alert alert-danger alert-validation'>"
																								+ response.message
																								+ "</div>")
																			}
																			$(
																					"img.captcha-img",
																					$form)
																					.click()
																		}
																	} else {
																		$actions
																				.before("<div class='alert alert-danger alert-validation'>E03: Form processing exceptions, please contact your administrator</div>");
																		$(
																				"img.captcha-img",
																				$form)
																				.click()
																	}
																}
															},
															error : function(
																	xhr, e,
																	status) {
																submitButton
																		.attr(
																				"disabled",
																				false);
																App
																		.unblockUI($form);
																$form
																		.find(
																				"input[name='token']")
																		.val(
																				new Date()
																						.getTime());
																$(
																		"img.captcha-img",
																		$form)
																		.click();
																try {
																	var message = "E04: Form processing exceptions, please contact your administrator";
																	var response = jQuery
																			.parseJSON(xhr.responseText);
																	if (response.type == "failure"
																			|| response.type == "error") {
																		message = response.message;
																		Global
																				.notify(
																						"error",
																						message)
																	}
																} catch (e) {
																	Global
																			.notify(
																					"error",
																					"Data Handling exceptions")
																}
																$actions
																		.before("<div class='alert alert-danger alert-validation'>"
																				+ message
																				+ "</div>")
															}
														});
												return false
											}
										});
								$form.data("validator", validator);
								$form
										.find(
												"input[type='text'],input[type='password'],select,textarea")
										.each(
												function() {
													if (!$(this).hasClass(
															"form-control")) {
														$(this).addClass(
																"form-control")
													}
												});
								$form
										.find(
												"input[type='text'],input[type='password'],select,textarea,hidden")
										.each(
												function() {
													var $el = $(this);
													if ($el
															.attr("data-tooltips")) {
														var $tips = $('<span class="glyphicon glyphicon-exclamation-sign tooltipster"  title="'
																+ $el
																		.attr("data-tooltips")
																+ '"></span>');
														$tips
																.tooltipster({
																	contentAsHTML : true,
																	offsetY : 5,
																	theme : "tooltipster-punk"
																});
														$el
																.closest(
																		".form-group")
																.find(
																		".control-label:first")
																.append($tips)
													}
												});
								if (options.editrulesurl
										&& options.editrulesurl != "false") {
									$form
											.ajaxJsonUrl(
													options.editrulesurl,
													function(json) {
														for ( var key in json) {
															var $el = $form
																	.find("[name='"
																			+ key
																			+ "']");
															if ($el.size() == 0) {
																continue
															}
															var rules = json[key];
															if (rules.tooltips) {
																var $tips = $('<span class="glyphicon glyphicon-exclamation-sign tooltipster"  title="'
																		+ rules.tooltips
																		+ '"></span>');
																$tips
																		.tooltipster({
																			contentAsHTML : true,
																			offsetY : 5,
																			theme : "tooltipster-punk"
																		});
																delete rules.tooltips;
																$el
																		.closest(
																				".form-group")
																		.find(
																				".control-label:first")
																		.append(
																				$tips)
															}
															if (rules.readonly) {
																var $id = $form
																		.find("input[name='id']");
																if ($el
																		.attr("readonly") == undefined
																		&& $id
																		&& $id
																				.val() != "") {
																	$el
																			.attr(
																					"readonly",
																					true)
																}
																delete rules.readonly
															}
															if (rules.required) {
																$el
																		.closest(
																				".form-group")
																		.find(
																				".control-label:first")
																		.append(
																				'<span class="required">*</span>')
															}
															for ( var rule in rules) {
																if ($el
																		.attr("data-rule-"
																				+ rule)) {
																	delete rules[rule]
																}
																if (!$.validator.methods[rule]) {
																	Global
																			.notify(
																					"error",
																					"Undefined form validation rules："
																							+ rule);
																	delete rules[rule]
																}
															}
															$el.rules("add",
																	rules)
														}
													})
								}
								$(".select2me", $form).change(function() {
									$form.validate().element($(this))
								});
								if ($form.attr("method") != undefined
										&& $form.attr("method").toUpperCase() == "POST") {
									$form.keypress(function(event) {
										var ta = event.target;
										if (ta.tagName === "TEXTAREA") {
											return true
										}
										return event.which != 13
									})
								}
								if (options && options.bindEvents) {
									options.bindEvents.call($form, {})
								}
								$form
										.on(
												"click",
												":submit",
												function(event) {
													$(
															'table[data-grid="items"]',
															$form)
															.each(
																	function() {
																		var $grid = $(this);
																		if ($grid
																				.jqGrid(
																						"isEditingMode",
																						true)) {
																			event
																					.preventDefault();
																			event
																					.stopPropagation();
																			return false
																		}
																	});
													if (options
															&& options.preValidate) {
														var validate = options.preValidate
																.call($form);
														if (validate != undefined
																&& validate == false) {
															event
																	.preventDefault();
															event
																	.stopPropagation();
															return false
														}
													}
													return true
												});
								if ($form.hasClass("form-search-init")) {
									$form.submit()
								}
							})
		},
		doSomeStuff : function() {
			myFunc()
		}
	}
}();