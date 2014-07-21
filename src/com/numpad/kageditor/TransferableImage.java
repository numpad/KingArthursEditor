package com.numpad.kageditor;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/*
 * to copy image to clipboard
 */
public class TransferableImage implements Transferable {
	
	private Image image;
	
	public TransferableImage(Image i) {
		this.image = i;
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(DataFlavor.imageFlavor) && image != null) {
			return image;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
	
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[1];
		flavors[0] = DataFlavor.imageFlavor;
		return flavors;
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavor.equals(flavors[i])) {
				return true;
			}
		}

		return false;
	}
}