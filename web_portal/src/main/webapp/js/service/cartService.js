//购物车服务层
app.service('cartService',function($http){
	//购物车列表
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');
	}
	
	//添加商品到购物车
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}

    this.dele = function(id){
        return $http.get("../cart/collection.do?id="+id);
    }


	// 批量添加关注
    this.addGoodsToCollection = function(ids){
		alert("service");
        return $http.get('cart/addGoodsToCollection.do?ids='+ids);
    }

    // 添加单个商品到关注
    this.addToCollection = function(ids){
        alert("service");
        return $http.get('cart/addGoodsToCollection.do?ids='+ids);
    }




	//求合计数
	this.sum=function(cartList){
		var totalValue={totalNum:0,totalMoney:0 };
			
		for(var i=0;i<cartList.length ;i++){
			var cart=cartList[i];//购物车对象
			for(var j=0;j<cart.orderItemList.length;j++){
				var orderItem=  cart.orderItemList[j];//购物车明细
				totalValue.totalNum+=orderItem.num;//累加数量
				totalValue.totalMoney+=orderItem.totalFee;//累加金额				
			}			
		}
		return totalValue;
		
	}
	
	//获取当前登录账号的收货地址
	this.findAddressList=function(){
		return $http.get('address/findListByLoginUser.do');
	}
	
	//提交订单
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);		
	}
	
	
});