app.controller("orderInfoController", function ($scope, cartService, addressService) {
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        })
    };

    //加载购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            //计算商品的总价和总数量
            $scope.totalValue = cartService.sumTotalValue(response);
        })
    };
    //根据用户名称加载用户地址列表
    $scope.findAddress = function () {
        addressService.findAddress().success(function (response) {
            $scope.addressList = response;
            //查询默认地址
            for (var i = 0; i < $scope.addressList.length; i++) {
                if ("1" == $scope.addressList[i].isDefault) {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
        })
    };
    //判断当前地址是否被选中
    $scope.isAddressSelected = function (address) {
        if ($scope.address == address) {
            return true;
        } else {
            return false;
        }
    };
    //选中地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
        // alert(JSON.stringify(address))
    };
    //订单
    $scope.order = {"paymentType": "1"}
    // 选中付款方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type
    }

    //提交订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;
        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success) {
                if ($scope.order.paymentType == "1") {
                    location.href = "pay.html#?outTradeNo=" + response.message;
                } else {
                    location.href = "paysuccess.html"
                }
            } else {
                alert(response.message)
            }
        })
    }
})