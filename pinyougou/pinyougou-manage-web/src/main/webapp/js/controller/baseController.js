app.controller("baseController", function ($scope) {
    //初始化分页导航条的信息
    $scope.paginationConf = {
        //页号
        currentPage: 1,
        //页大小
        itemsPerPage: 10,
        //总记录数
        totalItems: 0,
        //每页页大小选择
        perPageOptions: [10, 20, 30, 40, 50],
        //改变页号之后加载事件
        onChange: function () {
            $scope.reloadList();
        }
    };
    //重新调用search方法
    $scope.reloadList = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
    //重新调用search方法。更改当前页为1
    $scope.reloadList2 = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.paginationConf.currentPage = 1;
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
    //重新调用search方法。 设置查询对象为空。更改当前页为1
    $scope.reloadList3 = function () {
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.searchEntity = {};
        $scope.paginationConf.currentPage = 1;
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
    //重新调用search方法。设置查询对象为空。
    $scope.reloadList4 = function () {
        $scope.searchEntity = {};
        //$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };
    $scope.selectedIds = [];
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectedIds.push(id)
        } else {
            var index = $scope.selectedIds.indexOf(id);
            //删除位置，删除个数
            $scope.selectedIds.splice(index, 1);
        }
    };
    $scope.jsonToString = function (jsonStr, key) {
        var str = ""
        var jsonArray = JSON.parse(jsonStr);
        for (var i = 0; i < jsonArray.length; i++) {
            if (i > 0) {
                str += ","
            }
            str += jsonArray[i][key];
        }
        return str;
    };
})
