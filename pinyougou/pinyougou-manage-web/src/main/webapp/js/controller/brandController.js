app.controller("brandController", function ($scope, $http, $controller, brandService) {
    //继承一个controller
    $controller("baseController",{$scope:$scope})
    $scope.findAll = function () {
        brandService.findAll.success(function (data) {
            $scope.list = data;
        }).error(function () {
            alert("加载数据失败")
        })
    };
    $scope.searchEntity = {};
    $scope.search = function (page, rows) {
        brandService.search(page, rows, $scope.searchEntity).success(function (response) {
            //response 分页结果对象total,rows
            $scope.list = response.rows;
            //总记录数
            $scope.paginationConf.totalItems = response.total;

        });

    };
    //根据分页信息查询
    $scope.findPage = function (page, rows) {
        brandService.findPage(page, rows, $scope.searchEntity).success(function (response) {
            //response 分页结果对象total,rows
            $scope.list = response.rows;
            //总记录数
            $scope.paginationConf.totalItems = response.total;

        });

    };
    $scope.add = function () {
        var obj;
        if ($scope.entity.id != null) {
            obj = brandService.update($scope.entity)
        } else {
            obj = brandService.add($scope.entity)
        }
        obj.success(function (response) {
            if (response.success) {
                $scope.reloadList();
            } else {
                alert(response.message);
            }
        })
    }
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        })
    }

    $scope.delete = function () {
        if ($scope.selectedIds.length < 1) {
            alert("请选择要删除的品牌");
            return;
        }
        if (confirm("确定要删除选中的记录吗？ ")) {
            brandService.delete($scope.selectedIds).success(function (response) {
                if (response.success) {
                    $scope.reloadList();
                    $scope.selectedIds = [];
                } else {
                    alert(response.message)
                }
            });
        }
    }
})