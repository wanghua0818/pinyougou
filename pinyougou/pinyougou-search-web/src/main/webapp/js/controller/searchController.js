app.controller("searchController", function ($scope,$location, searchService) {
    //定义一个搜索条件对象
    $scope.searchMap = {"keywords": "", "category": "", "brand": "", "spec": {}, "price": "", "pageNo": 1, "pageSize": 20, "sortField": "", "sort": ""}
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            buildPageInfo();
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
        $scope.searchMap.pageNo = 1;
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
        $scope.searchMap.pageNo = 1;
        $scope.search();
    }
    //构建页面分页导航条信息
    buildPageInfo = function () {
        //定义要在页面显示的集合
        $scope.pageNoList = [];
        //定义要在分页导航条要显示的数量
        var showPageNoTotal = 5;
        //定义起始页号
        var startPageNo = 1;
        //结束页号
        var endPageNo = $scope.resultMap.totalPages;
        ////如果总页数大于要显示的页数才有需要处理显示页号数；否则直接显示所有页号
        if ($scope.resultMap.totalPages > showPageNoTotal) {
            // 计算当前页左右分割数
            var interval = Math.floor(showPageNoTotal / 2);
            //根据间隔得出起始，结束页号
            startPageNo = parseInt($scope.searchMap.pageNo) - interval;
            endPageNo = parseInt($scope.searchMap.pageNo) + interval;
            if (startPageNo > 0) {
                if (endPageNo > $scope.resultMap.totalPages) {
                    startPageNo = $scope.resultMap.totalPages - showPageNoTotal + 1;
                    endPageNo = $scope.resultMap.totalPages;
                }
            } else {
                startPageNo = 1;
                endPageNo = showPageNoTotal;


            }
        }
        //前面3个点，如果起始页大于1则存在
        $scope.frontDot = false;
        if (startPageNo > 1) {
            $scope.frontDot = true;
        }
        //后面3个点，如果结束页号页小于总页数则存在
        $scope.backDot = false;
        if (endPageNo < $scope.resultMap.totalPages) {
            $scope.backDot = true;
        }
        //获取正确的页号
        //获得正确的页号
        for (var i = startPageNo; i <= endPageNo; i++) {
            $scope.pageNoList.push(i);
        }
    }
    //是否是当前页
    $scope.isCurrentPage = function (pn) {
        return $scope.searchMap.pageNo == pn;
    }
    //查询指定页数 ,跳转到指定的页面
    $scope.queryByPageNo = function (pageNo) {
        pageNo = parseInt(pageNo)
        if (pageNo > 0 && pageNo <= $scope.resultMap.totalPages) {
            $scope.searchMap.pageNo = pageNo;
            $scope.search();
        }
    }
    //设置排序
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }
    //页面跳转处理
    $scope.loadKeyWords=function () {
        $scope.searchMap.keywords = $location.search()["keywords"];
        $scope.search();
    }
})