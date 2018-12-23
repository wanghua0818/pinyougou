app.controller("searchController", function ($scope, searchService) {
    //定义一个搜索条件对象
    $scope.searchMap = {"keywords": ""}
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
        })
    }
})