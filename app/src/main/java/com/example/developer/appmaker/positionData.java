package com.example.developer.appmaker;

public class positionData {
    private String[] positionInsertSqlArray;
    public positionData(){
        positionInsertSqlArray= new String[]{
                //'위치명',lat,lng
                "'강원대후문', 37.872654,127.744722",
                "'강원대정문', 37.866688,127.738254",
                "'한림대',37.886203,127.737813",
                "'애막골', 37.866203,127.752212",
                "'팔호광장',37.876566,127.735825",
                "'명동',37.879596,127.727577",
                "'퇴계동',37.850881,127.744389",
                "'스무숲',37.849594,127.749472",
                "'춘천역',37.884649,127.717794",
                "'남춘천역',37.864091,127.723799"
        };

    }
    public String[] getPositionInsertSql(){
        return positionInsertSqlArray;
    }
}
