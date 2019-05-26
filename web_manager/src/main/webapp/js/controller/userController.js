 //控制层 
app.controller('userController' ,function($scope,$controller ,userService){

    $controller('baseController',{$scope:$scope});//继承
	
	//注册用户
	$scope.reg=function(){
		
		//比较两次输入的密码是否一致
		if($scope.password!=$scope.entity.password){
			alert("两次输入密码不一致，请重新输入");
			$scope.entity.password="";
			$scope.password="";
			return ;			
		}
		//新增
		userService.add($scope.entity,$scope.smscode).success(
			function(response){
				alert(response.message);
			}		
		);
	}
    
	//发送验证码
	$scope.sendCode=function(){
		if($scope.entity.phone==null || $scope.entity.phone==""){
			alert("请填写手机号码");
			return ;
		}
		
		userService.sendCode($scope.entity.phone  ).success(
			function(response){
				alert(response.message);
			}
		);		
	}

    // 显示状态
    $scope.status = ["未审核","解除冻结","冻结","关闭"];
    // 审核的方法:,
    $scope.updateStatus=function (status) {
        userService.updateStatus(status,$scope.selectIds).success(function (response) {

            alert(response.message);
            $scope.reloadList();
        })
    }


    $scope.searchEntity={};//定义搜索对象

    //搜索
    $scope.search=function(page,rows){
        userService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
	
});	
