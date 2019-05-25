app.controller("contentController",function($scope,contentService){
	$scope.contentList = [];

	// 根据分类ID查询广告的方法:
	$scope.findByCategoryId = function(categoryId){
		contentService.findByCategoryId(categoryId).success(function(response){
			$scope.contentList[categoryId] = response;
		});
	}

    // 根据分类ID查询广告的方法:
    $scope.findByParentId = function(parentId){
        contentService.findByParentId(parentId).success(function(response){
            $scope.list=response;
        });
    }



	
	//搜索,跳转到portal系统查询列表页面(传递参数）
	$scope.search=function(){
		location.href="http://localhost:8080/search.html#?keywords="+$scope.keywords;
	}
	
});