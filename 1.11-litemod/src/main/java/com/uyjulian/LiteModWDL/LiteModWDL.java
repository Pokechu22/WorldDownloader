package com.uyjulian.LiteModWDL;

import java.io.File;
import com.mumfrey.liteloader.LiteMod;

public class LiteModWDL implements LiteMod {

	@Override
	public String getName() {
		return "LiteModWDL";
	}

	@Override
	public String getVersion() {
		return "1.11";
	}

	@Override
	public void init(File configPath) {

	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {
		
	}
	

}
