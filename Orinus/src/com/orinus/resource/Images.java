/*
 *  Orinus - JavaScript SandBox
 * 
 *  Copyright (c) 2011 Tran Dinh Thoai <dthoai@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.orinus.resource;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class Images {

	private static ImageRegistry registry;
	
	private static void setup() {
		if (registry != null) return;
		
		registry = new ImageRegistry();
		Class<?> baseClass = Images.class;

        registry.put("Icon.Orinus", ImageDescriptor.createFromFile(baseClass, "images/orinus-16x16.png"));
	}
	
	public static Image get(String key) {
		setup();
		return registry.get(key);
	}
	
}
