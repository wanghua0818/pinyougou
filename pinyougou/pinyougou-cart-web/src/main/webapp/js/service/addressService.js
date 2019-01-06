app.service("addressService", function ($http) {

    this.findCartList = function () {
        return $http.get("cart/findCartList.do?t=" + Math.random());

    };
    this.addItemToCartList = function (itemId, num) {
        return $http.get("cart/addItemToCartList.do?itemId=" + itemId + "&num=" + num + "&t=" + Math.random());

    };
    //查找地址列表
    this.findAddress = function () {
        return $http.get("address/findAddress.do?t=" + Math.random())
    }
})