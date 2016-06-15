<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Editing configuration parameters</title>
</head>
<body>
	<form:form class="form-horizontal form-bordered form-label-stripped form-validation"
		action="${ctx}/admin/sys/config-property/edit" method="post" modelAttribute="entity"
		data-editrulesurl="${ctx}/admin/util/validate?clazz=${clazz}">
		<form:hidden path="id" />
		<div class="form-actions">
			<button class="btn green" type="submit" data-grid-reload="#grid-sys-config-property-index" data-post-dismiss="modal">Save</button>
			<button class="btn default" type="button" data-dismiss="modal">cancel</button>
		</div>
		<div class="form-body">
			<div class="row">
				<div class="col-md-12">
					<div class="form-group">
						<label class="control-label">Code</label>
						<div class="controls">
							<form:input path="propKey" class="form-control" />
							<div class="help-block">Code is generally referenced in the program , do not arbitrarily change the code</div>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="form-group">
						<label class="control-label">name</label>
						<div class="controls">
							<form:input path="propName" class="form-control" />
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="form-group">
						<label class="control-label">Simple attribute value</label>
						<div class="controls">
							<form:input path="simpleValue" class="form-control" />
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="form-group">
						<label class="control-label">HTML attribute values</label>
						<div class="controls">
							<form:textarea path="htmlValue" class="form-control" data-htmleditor="kindeditor" data-height="400px"
								id="htmlValue" />
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">
					<div class="form-group">
						<label class="control-label">Parameter attribute Usage Notes</label>
						<div class="controls">
							<form:textarea path="propDescn" class="form-control" />
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="form-actions right">
			<button class="btn green" type="submit" data-grid-reload="#grid-sys-config-property-index" data-post-dismiss="modal">Save</button>
			<button class="btn default" type="button" data-dismiss="modal">Cancel</button>
		</div>
	</form:form>
</body>
</html>
