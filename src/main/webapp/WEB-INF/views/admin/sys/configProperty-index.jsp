<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Configuration Properties Management</title>
</head>
<body>
	<div class="row search-form-default">
		<div class="col-md-12">
			<form method="get" class="form-inline form-validation form-search-init control-label-sm"
				data-grid-search="#grid-sys-config-property-index">
				<div class="form-group">
					<div class="controls controls-clearfix">
						<input type="text" name="search['CN_propKey_OR_propName_OR_simpleValue']" class="form-control input-large"
							placeholder="Code, name, parameter values...">
					</div>
				</div>
				<div class="form-group search-group-btn">
					<button class="btn green" type="submmit">
						<i class="m-icon-swapright m-icon-white"></i>&nbsp; Charles &nbsp; Inquiry
					</button>
					<button class="btn default" type="reset">
						<i class="fa fa-undo"></i>&nbsp; Heavy &nbsp; Set
					</button>
				</div>
			</form>
		</div>
	</div>
	<div class="row">
		<div class="col-md-12">
			<table id="grid-sys-config-property-index"></table>
		</div>
	</div>

	<script type="text/javascript">
        $(function() {
            $("#grid-sys-config-property-index").data("gridOptions", {
                url : WEB_ROOT + '/admin/sys/config-property/list',
                colModel : [ {
                    label : 'Code',
                    name : 'propKey',
                    width : 120,
                    editable : true,
                    align : 'left'
                }, {
                    label : 'name',
                    name : 'propName',
                    width : 100,
                    editable : true,
                }, {
                    label : 'Simple attribute value',
                    name : 'simpleValue',
                    width : 80,
                    editable : true
                }, {
                    label : 'Parameter attribute Usage Notes',
                    name : 'propDescn',
                    sortable : false,
                    editable : true,
                    width : 200,
                    edittype : 'textarea'
                } ],
                editurl : WEB_ROOT + '/admin/sys/config-property/edit',
                editrulesurl : WEB_ROOT + '/admin/util/validate?clazz=${clazz}',
                fullediturl : WEB_ROOT + "/admin/sys/config-property/edit-tabs",
                delurl : WEB_ROOT + '/admin/sys/config-property/delete'
            });
        });
    </script>
</body>
</html>