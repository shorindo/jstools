/*
 * Copyright 2015 Shorindo, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shorindo.jstools;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class Paginator {
    public static final int MAX_INDICES = 7;
    public static final int MAX_ROWS = 10;
    private int numPages;
    
    /**
     * ページ分割処理クラス
     * @param totalRecords 全レコード数
     */
    public Paginator(int totalRecords) {
        numPages = totalRecords <= MAX_ROWS ? 1 :
            (int)Math.ceil((double)totalRecords / (double)MAX_ROWS);
    }
    
    /**
     * case 1: 全ページ数が最大インデックス数以下
     *         <1> [2] [3] [4] [5] [6] [7]
     * case 2: 指定ページが 最大インデックス数 - 1 より小さい
     *         [1] [2] [3] [4] <5> [-] [8]
     * case 3: 指定ページが 全ページ数 - (最大インデックス数 - 2) より大きい
     *         [1] [-] [4] [5] <6> [7] [8]
     * case 4: 上記以外
     *         [1] [-] [4] <5> [6] [-] [9]
     */
    public List<Integer> getIndices(int page) {
        List<Integer> resultList = new ArrayList<Integer>();
        page = normalizePage(page);

        // case 1
        if (numPages <= MAX_INDICES) {
            for (int i = 1; i <= numPages; i++) {
                resultList.add(i);
            }
            return resultList;
        }
        
        // case 2
        if (page < MAX_INDICES - 1) {
            for (int i = 1; i <= MAX_INDICES - 2; i++) {
                resultList.add(i);
            }
            resultList.add(- (MAX_INDICES - 1));
            resultList.add(numPages);
            return resultList;
        }
        
        // case 3
        if (page > numPages - (MAX_INDICES - 2)) {
            resultList.add(1);
            resultList.add(- (numPages - (MAX_INDICES - 2)));
            for (int i = numPages - (MAX_INDICES - 3); i <= numPages; i++) {
                resultList.add(i);
            }
            return resultList;
        }

        // case 4
        int begin = page - (MAX_INDICES - 2) / 2;
        int end   = begin + MAX_INDICES - 3;
        resultList.add(1);
        resultList.add(-begin);
        for (int i = begin + 1; i < end; i++) {
            resultList.add(i);
        }
        resultList.add(-end);
        resultList.add(numPages);
        return resultList;
    }
    
    public int getPrev(int page) {
        page = normalizePage(page);
        return page == 1 ? 0 : page - 1;
    }
    
    public int getNext(int page) {
        page = normalizePage(page);
        return page == numPages ? 0 : page + 1;
    }

    /**
     * 範囲外を正規化
     * @param page
     * @return
     */
    private int normalizePage(int page) {
        if (page < 1) {
            return 1;
        } else if (page > numPages) {
            return numPages;
        } else {
            return page;
        }
    }
    
}
