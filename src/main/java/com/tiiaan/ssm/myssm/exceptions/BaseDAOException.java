package com.tiiaan.ssm.myssm.exceptions;

/**
 * @author tiiaan Email:tiiaan.w@gmail.com
 * @version 1.0
 * description TODO
 */

public class BaseDAOException extends RuntimeException {
    static final long serialVersionUID = 3456772689764858957L;

    public BaseDAOException(String message) {
        super(message);
    }
}
