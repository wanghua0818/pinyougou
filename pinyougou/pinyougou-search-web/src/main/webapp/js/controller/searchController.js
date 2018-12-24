app.controller("searchController", function ($scope, searchService) {
    //定义一个搜索条件对象
    $scope.searchMap = {"keywords": "", "category": "", "brand": "", "spec": {}, "price": ""}
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
        })
    }
    //添加查询过滤条件
    $scope.addSearchItem = function (key, value) {
        if ("brand" == key || "category" == key || "price" == key) {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }

        //重新搜索
        $scope.search();
    }
    //撤销过滤条件
    $scope.removeSearchItem = function (key) {
        if (key == "brand" || key == "category" || key == "price") {
            $scope.searchMap[key] = ''
        } else {
            delete $scope.searchMap.spec[key]
        }
        //重新搜索
        $scope.search();
    }
})