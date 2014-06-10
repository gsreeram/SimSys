package edu.utdallas.sharedfiles.Challenge;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import edu.utdallas.sharedfiles.Shared.Asset;

@XmlRootElement(name = "Introduction")
public class Introduction 
{
	private String name;
    private List<Asset> assets;

	@XmlElement(name = "Name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

    @XmlElementWrapper(name = "Assets")
    @XmlElement(name = "AssetBase")
	public List<Asset> getAssets() {
		return assets;
	}

	public void setAssets(List<Asset> assets) {
		this.assets = assets;
	}
}
