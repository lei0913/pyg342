//购物车控制层
app.controller('cartController',function($scope,cartService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				$scope.totalValue= cartService.sum($scope.cartList);
			}
		);
	}
	
	//数量加减
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){//如果成功
					$scope.findCartList();//刷新列表
				}else{
					alert(response.message);
				}				
			}		
		);		
	}
	

	
	//获取当前用户的地址列表
	$scope.findAddressList=function(){
		cartService.findAddressList().success(
			function(response){
				$scope.addressList=response;
				for(var i=0;i<$scope.addressList.length;i++){
					if($scope.addressList[i].isDefault=='1'){
						$scope.address=$scope.addressList[i];
						break;
					}					
				}
				
			}
		);		
	}
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.address=address;		
	}
	
	//判断某地址对象是不是当前选择的地址
	$scope.isSeletedAddress=function(address){
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}		
	}
	
	$scope.order={paymentType:'1'};//订单对象
	
	//选择支付类型
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	//保存订单
	$scope.submitOrder=function(){
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder( $scope.order ).success(
			function(response){
				//alert(response.message);
				if(response.success){
					//页面跳转
					if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
						location.href="pay.html";
					}else{//如果货到付款，跳转到提示页面
						location.href="paysuccess.html";
					}
					
				}else{
					alert(response.message);	//也可以跳转到提示页面				
				}
				
			}				
		);		
	}



    // 定义一个数组:
    $scope.selectIds = [];
    // 更新复选框：
    $scope.updateSelection = function($event,id){
    	alert(id);
        // 复选框选中
        if($event.target.checked){
            // 向数组中添加元素
            $scope.selectIds.push(id);
        }else{
            // 从数组中移除
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);
        }

    }
    // 删除品牌:
    $scope.dele = function(){
        brandService.collection().success(function(response){
            // 判断保存是否成功:
            if(response.success==true){

                // 保存成功
                // alert(response.message);
                $scope.reloadList();
                $scope.selectIds = [];
            }else{
                // 保存失败
                alert(response.message);
            }
        });
    }

    // 批量添加关注
    $scope.addGoodsToCollection = function(id){
    	alert("2112");

        cartService.addGoodsToCollection($scope.selectIds).success(function(response){
        	alert("2222");
        	if (response.success==true){
        		alert(response.message);
//     $scope.reloadList();		//重新加载当前列表
//     $scope.selectIds = [];		//重新定义一个数组，清除原来选中的数据
			}else {
        		alert(response.message);
			}
        });
    }


    // 添加单个商品到关注
    $scope.addToCollection = function(id){
        alert("2112");
        alert(id);

        $scope.selectIds = [];
        $scope.selectIds.push(id);
        alert("12");
        cartService.addToCollection($scope.selectIds).success(function(response){
            alert("2222");
            if (response.success==true){
                alert(response.message);
//     $scope.reloadList();		//重新加载当前列表
//     $scope.selectIds = [];		//重新定义一个数组，清除原来选中的数据
            }else {
                alert(response.message);
            }
        });
    }

	
});