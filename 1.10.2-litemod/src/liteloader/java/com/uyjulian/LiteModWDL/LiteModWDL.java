package com.uyjulian.LiteModWDL;

import java.io.File;
import com.mumfrey.liteloader.LiteMod;
import wdl.VersionConstants;

public class LiteModWDL implements LiteMod {

	@Override
	public String getName() {
		return "LiteModWDL";
	}

	@Override
	public String getVersion() {
		return VersionConstants.getModVersion() + "-" + VersionConstants.getExpectedVersion();
	}

	@Override
	public void init(File configPath) {

	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {
		
	}
	

}
