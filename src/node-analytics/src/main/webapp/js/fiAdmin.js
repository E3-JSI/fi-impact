(function () {
	'use strict';
	angular.module('fiManagerApp', ['ui.bootstrap']);
}());

(function () {
	'use strict';
	angular.module('fiManagerApp').filter("sanitize", ['$sce', function($sce) {
		return function(htmlCode) { return $sce.trustAsHtml(htmlCode); }
	}]);
}());

(function () {
	'use strict';
	angular.module('fiManagerApp').service('formSubmit', ['$http', '$q', '$httpParamSerializerJQLike',  function ($http, $q, $httpParamSerializerJQLike) {
		var fixSerializedArray = function(serialized) { return serialized.replace(/%5B%5D/g, '') }
		return {
			async: function(action, data, postprocess) {
				data.action = action
				var promise = $http.post("../../manager", fixSerializedArray($httpParamSerializerJQLike(data)), {
					transformRequest: angular.identity,
					headers: {'Content-Type': 'application/x-www-form-urlencoded'}
				}).then(postprocess)
			}
		}
	}]);
}());
