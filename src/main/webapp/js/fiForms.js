(function () {
	'use strict';
	angular.module('uploadApp', ['ui.bootstrap']);
}());

(function () {
	'use strict';
	angular.module('uploadApp').directive('fileModel', ['$parse', function ($parse) {
		return {
			restrict: 'A',
			link: function(scope, element, attrs) {
				var model = $parse(attrs.fileModel);
				var modelSetter = model.assign;
				element.bind('change', function(){
					scope.$apply(function(){ modelSetter(scope, element[0].files[0]) });
				});
			}
		};
	}]);
}());


(function () {
	'use strict';
	angular.module('uploadApp').service('fileUpload', ['$http', '$q', function($http, $q) {
		return {
			async: function(file, uploadUrl, postprocess) {
				var fd = new FormData();
				fd.append('file', file);
				fd.append('action', 'upload-mattermark')
				var promise = $http.post(uploadUrl, fd, {
					transformRequest: angular.identity,
					headers: {'Content-Type': undefined}
				}).then(postprocess)
			}
		}
	}])
}());

(function () {
	'use strict';
	angular.module('uploadApp').controller('ModalCtrl', ['$uibModalInstance', 'fileUpload', function ($uibModalInstance, fileUpload) {
		var vm = this;	
		vm.file = null
		vm.uploadButton = ['Continue', 'Upload File', 'Continue', 'Done']			
		vm.active = 0
		vm.model = { name: 'Tabs' }; 
		
		vm.action = function(tabId) {
			if (tabId == 3) {
				$uibModalInstance.close()
				vm.active = 0
			}
			else {
				vm.active = tabId+1
				if (tabId == 1) fileUpload.async(vm.file, "../../manager", function(response) {
					console.log(response)
					vm.active = vm.active+1
				})
			}
		}	

		vm.dismiss = function () {
			vm.file = null
			$uibModalInstance.dismiss();
		};
	}]);
}());