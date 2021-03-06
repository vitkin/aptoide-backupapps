/**
 * MyappParser, 	auxiliary class to Aptoide's ServiceData
 * Copyright (C) 2011 Duarte Silveira
 * duarte.silveira@caixamagica.pt
 * 
 * derivative work of previous Aptoide's RssHandler with
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package pt.aptoide.backupapps.data.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import pt.aptoide.backupapps.data.cache.ViewCache;
import pt.aptoide.backupapps.data.display.ViewDisplayListRepos;
import pt.aptoide.backupapps.data.display.ViewDisplayRepo;
import pt.aptoide.backupapps.data.listeners.ViewMyapp;
import pt.aptoide.backupapps.data.util.Constants;

import android.util.Log;

/**
 * MyappParser, handles myapp xml Sax parsing
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ParserMyapp extends DefaultHandler{
	private ManagerXml managerXml = null;
	
	private ViewCache myapp;
	private ViewMyapp viewMyapp = null;
	private ViewDisplayRepo repo = null;
	private ViewDisplayListRepos listRepos = new ViewDisplayListRepos(1);
	
	private EnumXmlTagsMyapp tag = EnumXmlTagsMyapp.myapp;
	
	private StringBuilder tagContentBuilder;
	
		
	public ParserMyapp(ManagerXml managerXml, ViewCache myapp){
		this.managerXml = managerXml;
		this.myapp = myapp;
	}
	
	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		super.characters(chars, start, length);
		
		tagContentBuilder.append(new String(chars, start, length).trim());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		
		tag = EnumXmlTagsMyapp.safeValueOf(localName.trim());	
		
		switch (tag) {
			case name:
				viewMyapp = new ViewMyapp(tagContentBuilder.toString());
				break;
			case pname:
				viewMyapp.setPackageName(tagContentBuilder.toString());
				break;
			case md5sum:
				viewMyapp.setMd5sum(tagContentBuilder.toString());
				break;
			case intsize:
				viewMyapp.setSize(Integer.parseInt(tagContentBuilder.toString()));
				break;
			case get:
				viewMyapp.setRemotePath(tagContentBuilder.toString());
				break;
				
			case getapp:
				Log.d("Aptoide-MyappParser", "myapp: "+viewMyapp);
				break;
			
				
			case server:
				repo = new ViewDisplayRepo(tagContentBuilder.toString().hashCode(), tagContentBuilder.toString(), false, Constants.EMPTY_INT);
				listRepos.addRepo(repo);
				break;
				
			case newserver:
				Log.d("Aptoide-MyappParser", "list of Repos: "+listRepos);
				break;
				
			default:
				break;
		}		
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		tagContentBuilder = new StringBuilder();
		
	}
	
	
	
	
	@Override
	public void startDocument() throws SAXException {	//TODO refacto Logs
		Log.d("Aptoide-MyappParser","Started parsing XML from " + myapp.getLocalPath() + " ...");
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d("Aptoide-RepoBareParser","Done parsing XML from " + myapp.getLocalPath() + " !");

		managerXml.parsingMyappFinished(viewMyapp, listRepos);
		super.endDocument();
	}


}
