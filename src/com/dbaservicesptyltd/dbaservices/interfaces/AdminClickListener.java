/**
 * 
 */
package com.dbaservicesptyltd.dbaservices.interfaces;

import com.dbaservicesptyltd.dbaservices.model.OnlineAdminRow;

/**
 * @author Touhid
 *
 */
public interface AdminClickListener {
	
	public void handleClick(boolean isAdd, OnlineAdminRow admin);

}
