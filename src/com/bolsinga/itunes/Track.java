package com.bolsinga.itunes;

import java.util.*;

class Track {
  private static final String TK_ALBUM                 = "Album";
  private static final String TK_ARTIST                = "Artist";
  private static final String TK_ARTWORK_COUNT         = "Artwork Count";
  private static final String TK_BIT_RATE              = "Bit Rate";
  private static final String TK_COMMENTS              = "Comments";
  private static final String TK_COMPILATION           = "Compilation";
  private static final String TK_COMPOSER              = "Composer";
  private static final String TK_DATE_ADDED            = "Date Added";
  private static final String TK_DATE_MODIFIED         = "Date Modified";
  private static final String TK_DISC_COUNT            = "Disc Count";
  private static final String TK_DISC_NUMBER           = "Disc Number";
  private static final String TK_FILE_CREATOR          = "File Creator";
  private static final String TK_FILE_FOLDER_COUNT     = "File Folder Count";
  private static final String TK_FILE_TYPE             = "File Type";
  private static final String TK_GENRE                 = "Genre";
  private static final String TK_KIND                  = "Kind";
  private static final String TK_LIBRARY_FOLDER_COUNT  = "Library Folder Count";
  private static final String TK_LOCATION              = "Location";
  private static final String TK_NAME                  = "Name";
  private static final String TK_PLAY_COUNT            = "Play Count";
  private static final String TK_PLAY_DATE             = "Play Date";
  private static final String TK_PLAY_DATE_UTC         = "Play Date UTC";
  private static final String TK_SAMPLE_RATE           = "Sample Rate";
  private static final String TK_SIZE                  = "Size";
  private static final String TK_TOTAL_TIME            = "Total Time";
  private static final String TK_TRACK_COUNT           = "Track Count";
  private static final String TK_TRACK_ID              = "Track ID";
  private static final String TK_TRACK_NUMBER          = "Track Number";
  private static final String TK_TRACK_TYPE            = "Track Type";
  private static final String TK_YEAR                  = "Year";
  private static final String TK_SEASON                = "Season";
  private static final String TK_PERSISTENT_ID         = "Persistent ID";
  private static final String TK_SERIES                = "Series";
  private static final String TK_EPISODE               = "Episode";
  private static final String TK_EPISODE_ORDER         = "Episode Order";
  private static final String TK_HAS_VIDEO             = "Has Video";
  private static final String TK_TV_SHOW               = "TV Show";
  private static final String TK_PROTECTED             = "Protected";
  private static final String TK_BPM                   = "BPM";
  private static final String TK_ALBUM_ARTIST          = "Album Artist";
  private static final String TK_EXPLICIT              = "Explicit";
  private static final String TK_SKIP_COUNT            = "Skip Count";
  private static final String TK_SKIP_DATE             = "Skip Date";
  private static final String TK_RELEASE_DATE          = "Release Date";
  private static final String TK_PODCAST               = "Podcast";
  private static final String TK_MOVIE                 = "Movie";
  private static final String TK_UNPLAYED              = "Unplayed";
  private static final String TK_SORT_ALBUM            = "Sort Album";
  private static final String TK_SORT_ALBUM_ARTIST     = "Sort Album Artist";
  private static final String TK_SORT_ARTIST           = "Sort Artist";
  private static final String TK_SORT_COMPOSER         = "Sort Composer";
  private static final String TK_SORT_NAME             = "Sort Name";
  private static final String TK_CONTENT_RATING        = "Content Rating";
  private static final String TK_DISABLED              = "Disabled";
  private static final String TK_PURCHASED             = "Purchased";
  private static final String TK_VIDEO_HEIGHT          = "Video Height";
  private static final String TK_VIDEO_WIDTH           = "Video Width";
  private static final String TK_EID                   = "EID";
  private static final String TK_HD                    = "HD";
  private static final String TK_ALBUM_RATING          = "Album Rating";
  private static final String TK_ALBUM_RATING_COMPUTED = "Album Rating Computed";
  private static final String TK_RATING                = "Rating";
  private static final String TK_GROUPING              = "Grouping";
  private static final String TK_PART_OF_GAPLESS_ALBUM = "Part Of Gapless Album";
  private static final String TK_MUSIC_VIDEO           = "Music Video";
  private static final String TK_MATCH_ID              = "MatchID";
  private static final String TK_XID                   = "XID";
  private static final String TK_RATING_COMPUTED       = "Rating Computed";
  private static final String TK_SORT_SERIES           = "Sort Series";

  private static final String TKIND_AAC_AUDIO_FILE = "AAC audio file";
  private static final String TKIND_BOOK = "Book";
  private static final String TKIND_INTERNET_AUDIO_STREAM = "Internet audio stream";
  private static final String TKIND_MPEG_AUDIO_FILE = "MPEG audio file";
  private static final String TKIND_MPEG_4_VIDEO_FILE = "MPEG-4 video file";
  private static final String TKIND_PDF_DOCUMENT = "PDF document";
  private static final String TKIND_PROTECTED_AAC_AUDIO_FILE = "Protected AAC audio file";
  private static final String TKIND_PROTECTED_MPEG_4_VIDEO_FILE = "Protected MPEG-4 video file";
  private static final String TKIND_PROTECTED_BOOK = "Protected book";
  private static final String TKIND_PURCHASED_AAC_AUDIO_FILE = "Purchased AAC audio file";
  private static final String TKIND_PURCHASED_MPEG_4_VIDEO_FILE = "Purchased MPEG-4 video file";
  private static final String TKIND_PURCHASED_BOOK = "Purchased book";
  private static final String TKIND_IPAD_APP = "iPad app";
  private static final String TKIND_IPHONE_IPOD_TOUCH_APP = "iPhone/iPod touch app";
  private static final String TKIND_IPHONE_IPOD_TOUCH_IPAD_APP = "iPhone/iPod touch/iPad app";
  private static final String TKIND_ITUNES_EXTRAS = "iTunes Extras";
  private static final String TKIND_MPEG_AUDIO_STREAM = "MPEG audio stream";

  private static final Set<String> sNewKeys = new TreeSet<String>();
  private static final HashSet<String> sKinds = new HashSet<String>();
  private static final Set<String> sNewKinds = new TreeSet<String>();
  private static final HashSet<String> sAudioKinds = new HashSet<String>();

  private static void createKnownKinds() {
    sKinds.add(TKIND_AAC_AUDIO_FILE);
    sKinds.add(TKIND_BOOK);
    sKinds.add(TKIND_INTERNET_AUDIO_STREAM);
    sKinds.add(TKIND_MPEG_AUDIO_FILE);
    sKinds.add(TKIND_MPEG_4_VIDEO_FILE);
    sKinds.add(TKIND_PDF_DOCUMENT);
    sKinds.add(TKIND_PROTECTED_AAC_AUDIO_FILE);
    sKinds.add(TKIND_PROTECTED_MPEG_4_VIDEO_FILE);
    sKinds.add(TKIND_PROTECTED_BOOK);
    sKinds.add(TKIND_PURCHASED_AAC_AUDIO_FILE);
    sKinds.add(TKIND_PURCHASED_MPEG_4_VIDEO_FILE);
    sKinds.add(TKIND_PURCHASED_BOOK);
    sKinds.add(TKIND_IPAD_APP);
    sKinds.add(TKIND_IPHONE_IPOD_TOUCH_APP);
    sKinds.add(TKIND_IPHONE_IPOD_TOUCH_IPAD_APP);
    sKinds.add(TKIND_ITUNES_EXTRAS);
    sKinds.add(TKIND_MPEG_AUDIO_STREAM);

    sAudioKinds.add(TKIND_AAC_AUDIO_FILE);
    sAudioKinds.add(TKIND_MPEG_AUDIO_FILE);
    sAudioKinds.add(TKIND_PROTECTED_AAC_AUDIO_FILE);
    sAudioKinds.add(TKIND_PURCHASED_AAC_AUDIO_FILE);
  }

  static {
    // Create a list of all known iTunes kinds. This way if a new one shows up, the program will let us know.
    Track.createKnownKinds();
  }

  private String Album;
  private String Artist;
  private String Artwork_Count;
  private String Bit_Rate;
  private String Comments;
  private String Compilation;
  private String Composer;
  private String Date_Added;
  private String Date_Modified;
  private String Disc_Count;
  private String Disc_Number;
  private String File_Creator;
  private String File_Folder_Count;
  private String File_Type;
  private String Genre;
  private String Kind;
  private String Library_Folder_Count;
  private String Location;
  private String Name;
  private String Play_Count;
  private String Play_Date;
  private String Play_Date_UTC;
  private String Sample_Rate;
  private String Size;
  private String Total_Time;
  private String Track_Count;
  private String Track_ID;
  private String Track_Number;
  private String Track_Type;
  private String Year;
  private String Season;
  private String Persistent_ID;
  private String Series;
  private String Episode;
  private String Episode_Order;
  private String Has_Video;
  private String TV_Show;
  private String Protected;
  private String BPM;
  private String Album_Artist;
  private String Explicit;
  private String Skip_Count;
  private String Skip_Date;
  private String Release_Date;
  private String Podcast;
  private String Movie;
  private String Unplayed;
  private String Sort_Album;
  private String Sort_Album_Artist;
  private String Sort_Artist;
  private String Sort_Composer;
  private String Sort_Name;
  private String Content_Rating;
  private String Disabled;
  private String Purchased;
  private String Video_Height;
  private String Video_Width;
  private String EID;
  private String HD;
  private String Album_Rating;
  private String Album_Rating_Computed;
  private String Rating;
  private String Grouping;
  private String Part_Of_Gapless_Album;
  private String Music_Video;
  private String MatchID;
  private String XID;
  private String Rating_Computed;
  private String Sort_Series;

  public String getAlbum() { return Album; } public void setAlbum(String Album) { this.Album = Album; }
  public String getArtist() { return Artist; } public void setArtist(String Artist) { this.Artist = Artist; }
  public String getArtwork_Count() { return Artwork_Count; } public void setArtwork_Count(String Artwork_Count) { this.Artwork_Count = Artwork_Count; }
  public String getBit_Rate() { return Bit_Rate; } public void setBit_Rate(String Bit_Rate) { this.Bit_Rate = Bit_Rate; }
  public String getComments() { return Comments; } public void setComments(String Comments) { this.Comments = Comments; }
  public String getCompilation() { return Compilation; } public void setCompilation(String Compilation) { this.Compilation = Compilation; }
  public String getComposer() { return Composer; } public void setComposer(String Composer) { this.Composer = Composer; }
  public String getDate_Added() { return Date_Added; } public void setDate_Added(String Date_Added) { this.Date_Added = Date_Added; }
  public String getDate_Modified() { return Date_Modified; } public void setDate_Modified(String Date_Modified) { this.Date_Modified = Date_Modified; }
  public String getDisc_Count() { return Disc_Count; } public void setDisc_Count(String Disc_Count) { this.Disc_Count = Disc_Count; }
  public String getDisc_Number() { return Disc_Number; } public void setDisc_Number(String Disc_Number) { this.Disc_Number = Disc_Number; }
  public String getFile_Creator() { return File_Creator; } public void setFile_Creator(String File_Creator) { this.File_Creator = File_Creator; }
  public String getFile_Folder_Count() { return File_Folder_Count; } public void setFile_Folder_Count(String File_Folder_Count) { this.File_Folder_Count = File_Folder_Count; }
  public String getFile_Type() { return File_Type; } public void setFile_Type(String File_Type) { this.File_Type = File_Type; }
  public String getGenre() { return Genre; } public void setGenre(String Genre) { this.Genre = Genre; }
  public String getKind() { return Kind; } public void setKind(String Kind) { this.Kind = Kind; if (!sKinds.contains(Kind)) { sNewKinds.add(Kind); } }
  public String getLibrary_Folder_Count() { return Library_Folder_Count; } public void setLibrary_Folder_Count(String Library_Folder_Count) { this.Library_Folder_Count = Library_Folder_Count; }
  public String getLocation() { return Location; } public void setLocation(String Location) { this.Location = Location; }
  public String getName() { return Name; } public void setName(String Name) { this.Name = Name; }
  public String getPlay_Count() { return Play_Count; } public void setPlay_Count(String Play_Count) { this.Play_Count = Play_Count; }
  public String getPlay_Date() { return Play_Date; } public void setPlay_Date(String Play_Date) { this.Play_Date = Play_Date; }
  public String getPlay_Date_UTC() { return Play_Date_UTC; } public void setPlay_Date_UTC(String Play_Date_UTC) { this.Play_Date_UTC = Play_Date_UTC; }
  public String getSample_Rate() { return Sample_Rate; } public void setSample_Rate(String Sample_Rate) { this.Sample_Rate = Sample_Rate; }
  public String getSize() { return Size; } public void setSize(String Size) { this.Size = Size; }
  public String getTotal_Time() { return Total_Time; } public void setTotal_Time(String Total_Time) { this.Total_Time = Total_Time; }
  public String getTrack_Count() { return Track_Count; } public void setTrack_Count(String Track_Count) { this.Track_Count = Track_Count; }
  public String getTrack_ID() { return Track_ID; } public void setTrack_ID(String Track_ID) { this.Track_ID = Track_ID; }
  public String getTrack_Number() { return Track_Number; } public void setTrack_Number(String Track_Number) { this.Track_Number = Track_Number; }
  public String getTrack_Type() { return Track_Type; } public void setTrack_Type(String Track_Type) { this.Track_Type = Track_Type; }
  public String getYear() { return Year; } public void setYear(String Year) { this.Year = Year; }
  public String getSeason() { return Season; } public void setSeason(String Season) { this.Season = Season; }
  public String getPersistent_ID() { return Persistent_ID; } public void setPersistent_ID(String Persistent_ID) { this.Persistent_ID = Persistent_ID; }
  public String getSeries() { return Series; } public void setSeries(String Series) { this.Series = Series; }
  public String getEpisode() { return Episode; } public void setEpisode(String Episode) { this.Episode = Episode; }
  public String getEpisode_Order() { return Episode_Order; } public void setEpisode_Order(String Episode_Order) { this.Episode_Order = Episode_Order; }
  public String getHas_Video() { return Has_Video; } public void setHas_Video(String Has_Video) { this.Has_Video = Has_Video; }
  public String getTV_Show() { return TV_Show; } public void setTV_Show(String TV_Show) { this.TV_Show = TV_Show; }
  public String getProtected() { return Protected; } public void setProtected(String Protected) { this.Protected = Protected; }
  public String getBPM() { return BPM; } public void setBPM(String BPM) { this.BPM = BPM; }
  public String getAlbum_Artist() { return Album_Artist; } public void setAlbum_Artist(String Album_Artist) { this.Album_Artist = Album_Artist; }
  public String getExplicit() { return Explicit; } public void setExplicit(String Explicit) { this.Explicit = Explicit; }
  public String getSkip_Count() { return Skip_Count; } public void setSkip_Count(String Skip_Count) { this.Skip_Count = Skip_Count; }
  public String getSkip_Date() { return Skip_Date; } public void setSkip_Date(String Skip_Date) { this.Skip_Date = Skip_Date; }
  public String getRelease_Date() { return Release_Date; } public void setRelease_Date(String Release_Date) { this.Release_Date = Release_Date; }
  public String getPodcast() { return Podcast; } public void setPodcast(String Podcast) { this.Podcast = Podcast; }
  public String getMovie() { return Movie; } public void setMovie(String Movie) { this.Movie = Movie; }
  public String getUnplayed() { return Unplayed; } public void setUnplayed(String Unplayed) { this.Unplayed = Unplayed; }
  public String getSort_Album() { return Sort_Album; } public void setSort_Album(String Sort_Album) { this.Sort_Album = Sort_Album; }
  public String getSort_Album_Artist() { return Sort_Album_Artist; } public void setSort_Album_Artist(String Sort_Album_Artist) { this.Sort_Album_Artist = Sort_Album_Artist; }
  public String getSort_Artist() { return Sort_Artist; } public void setSort_Artist(String Sort_Artist) { this.Sort_Artist = Sort_Artist; }
  public String getSort_Composer() { return Sort_Composer; } public void setSort_Composer(String Sort_Composer) { this.Sort_Composer = Sort_Composer; }
  public String getSort_Name() { return Sort_Name; } public void setSort_Name(String Sort_Name) { this.Sort_Name = Sort_Name; }
  public String getContent_Rating() { return Content_Rating; } public void setContent_Rating(String Content_Rating) { this.Content_Rating = Content_Rating; }
  public String getDisabled() { return Disabled; } public void setDisabled(String Disabled) { this.Disabled = Disabled; }
  public String getPurchased() { return Purchased; } public void setPurchased(String Purchased) { this.Purchased = Purchased; }
  public String getVideo_Height() { return Video_Height; } public void setVideo_Height(String Video_Height) { this.Video_Height = Video_Height; }
  public String getVideo_Width() { return Video_Width; } public void setVideo_Width(String Video_Width) { this.Video_Width = Video_Width; }
  public String getEID() { return EID; } public void setEID(String EID) { this.EID = EID; }
  public String getHD() { return HD; } public void setHD(String HD) { this.HD = HD; }
  public String getAlbum_Rating() { return Album_Rating; } public void setAlbum_Rating(String Album_Rating) { this.Album_Rating = Album_Rating; }
  public String getAlbum_Rating_Computed() { return Album_Rating_Computed; } public void setAlbum_Rating_Computed(String Album_Rating_Computed) { this.Album_Rating_Computed = Album_Rating_Computed; }
  public String getRating() { return Rating; } public void setRating(String Rating) { this.Rating = Rating; }
  public String getGrouping() { return Grouping; } public void setGrouping(String Grouping) { this.Grouping = Grouping; }
  public String getPart_Of_Gapless_Album() { return Part_Of_Gapless_Album; } public void setPart_Of_Gapless_Album(String Part_Of_Gapless_Album) { this.Part_Of_Gapless_Album = Part_Of_Gapless_Album; }
  public String getMusic_Video() { return Music_Video; } public void setMusic_Video(String Music_Video) { this.Music_Video = Music_Video; }
  public String getMatchID() { return MatchID; } public void setMatchID(String MatchID) { this.MatchID = MatchID; }
  public String getXID() { return XID; } public void setXID(String XID) { this.XID = XID; }
  public String getRating_Computed() { return Rating_Computed; } public void setRating_Computed(String Rating_Computed) { this.Rating_Computed = Rating_Computed; }
  public String getSort_Series() { return Sort_Series; } public void setSort_Series(String Sort_Series) { this.Sort_Series = Sort_Series; }

  public void set(String elementName, String value) {
    if (TK_ALBUM.equals(elementName)) { this.setAlbum(value); }
    else if (TK_ARTIST.equals(elementName)) { this.setArtist(value); }
    else if (TK_ARTWORK_COUNT.equals(elementName)) { this.setArtwork_Count(value); }
    else if (TK_BIT_RATE.equals(elementName)) { this.setBit_Rate(value); }
    else if (TK_COMMENTS.equals(elementName)) { this.setComments(value); }
    else if (TK_COMPILATION.equals(elementName)) { this.setCompilation(value); }
    else if (TK_COMPOSER.equals(elementName)) { this.setComposer(value); }
    else if (TK_DATE_ADDED.equals(elementName)) { this.setDate_Added(value); }
    else if (TK_DATE_MODIFIED.equals(elementName)) { this.setDate_Modified(value); }
    else if (TK_DISC_COUNT.equals(elementName)) { this.setDisc_Count(value); }
    else if (TK_DISC_NUMBER.equals(elementName)) { this.setDisc_Number(value); }
    else if (TK_FILE_CREATOR.equals(elementName)) { this.setFile_Creator(value); }
    else if (TK_FILE_FOLDER_COUNT.equals(elementName)) { this.setFile_Folder_Count(value); }
    else if (TK_FILE_TYPE.equals(elementName)) { this.setFile_Type(value); }
    else if (TK_GENRE.equals(elementName)) { this.setGenre(value); }
    else if (TK_KIND.equals(elementName)) { this.setKind(value); }
    else if (TK_LIBRARY_FOLDER_COUNT.equals(elementName)) { this.setLibrary_Folder_Count(value); }
    else if (TK_LOCATION.equals(elementName)) { this.setLocation(value); }
    else if (TK_NAME.equals(elementName)) { this.setName(value); }
    else if (TK_PLAY_COUNT.equals(elementName)) { this.setPlay_Count(value); }
    else if (TK_PLAY_DATE.equals(elementName)) { this.setPlay_Date(value); }
    else if (TK_PLAY_DATE_UTC.equals(elementName)) { this.setPlay_Date_UTC(value); }
    else if (TK_SAMPLE_RATE.equals(elementName)) { this.setSample_Rate(value); }
    else if (TK_SIZE.equals(elementName)) { this.setSize(value); }
    else if (TK_TOTAL_TIME.equals(elementName)) { this.setTotal_Time(value); }
    else if (TK_TRACK_COUNT.equals(elementName)) { this.setTrack_Count(value); }
    else if (TK_TRACK_ID.equals(elementName)) { this.setTrack_ID(value); }
    else if (TK_TRACK_NUMBER.equals(elementName)) { this.setTrack_Number(value); }
    else if (TK_TRACK_TYPE.equals(elementName)) { this.setTrack_Type(value); }
    else if (TK_YEAR.equals(elementName)) { this.setYear(value); }
    else if (TK_SEASON.equals(elementName)) { this.setSeason(value); }
    else if (TK_PERSISTENT_ID.equals(elementName)) { this.setPersistent_ID(value); }
    else if (TK_SERIES.equals(elementName)) { this.setSeries(value); }
    else if (TK_EPISODE.equals(elementName)) { this.setEpisode(value); }
    else if (TK_EPISODE_ORDER.equals(elementName)) { this.setEpisode_Order(value); }
    else if (TK_HAS_VIDEO.equals(elementName)) { this.setHas_Video(value); }
    else if (TK_TV_SHOW.equals(elementName)) { this.setTV_Show(value); }
    else if (TK_PROTECTED.equals(elementName)) { this.setProtected(value); }
    else if (TK_BPM.equals(elementName)) { this.setBPM(value); }
    else if (TK_ALBUM_ARTIST.equals(elementName)) { this.setAlbum_Artist(value); }
    else if (TK_EXPLICIT.equals(elementName)) { this.setExplicit(value); }
    else if (TK_SKIP_COUNT.equals(elementName)) { this.setSkip_Count(value); }
    else if (TK_SKIP_DATE.equals(elementName)) { this.setSkip_Date(value); }
    else if (TK_RELEASE_DATE.equals(elementName)) { this.setRelease_Date(value); }
    else if (TK_PODCAST.equals(elementName)) { this.setPodcast(value); }
    else if (TK_MOVIE.equals(elementName)) { this.setMovie(value); }
    else if (TK_UNPLAYED.equals(elementName)) { this.setUnplayed(value); }
    else if (TK_SORT_ALBUM.equals(elementName)) { this.setSort_Album(value); }
    else if (TK_SORT_ALBUM_ARTIST.equals(elementName)) { this.setSort_Album_Artist(value); }
    else if (TK_SORT_ARTIST.equals(elementName)) { this.setSort_Artist(value); }
    else if (TK_SORT_COMPOSER.equals(elementName)) { this.setSort_Composer(value); }
    else if (TK_SORT_NAME.equals(elementName)) { this.setSort_Name(value); }
    else if (TK_CONTENT_RATING.equals(elementName)) { this.setContent_Rating(value); }
    else if (TK_DISABLED.equals(elementName)) { this.setDisabled(value); }
    else if (TK_PURCHASED.equals(elementName)) { this.setPurchased(value); }
    else if (TK_VIDEO_HEIGHT.equals(elementName)) { this.setVideo_Height(value); }
    else if (TK_VIDEO_WIDTH.equals(elementName)) { this.setVideo_Width(value); }
    else if (TK_EID.equals(elementName)) { this.setEID(value); }
    else if (TK_HD.equals(elementName)) { this.setHD(value); }
    else if (TK_ALBUM_RATING.equals(elementName)) { this.setAlbum_Rating(value); }
    else if (TK_ALBUM_RATING_COMPUTED.equals(elementName)) { this.setAlbum_Rating_Computed(value); }
    else if (TK_RATING.equals(elementName)) { this.setRating(value); }
    else if (TK_GROUPING.equals(elementName)) { this.setGrouping(value); }
    else if (TK_PART_OF_GAPLESS_ALBUM.equals(elementName)) { this.setPart_Of_Gapless_Album(value); }
    else if (TK_MUSIC_VIDEO.equals(elementName)) { this.setMusic_Video(value); }
    else if (TK_MATCH_ID.equals(elementName)) { this.setMatchID(value); }
    else if (TK_XID.equals(elementName)) { this.setXID(value); }
    else if (TK_RATING_COMPUTED.equals(elementName)) { this.setRating_Computed(value); }
    else if (TK_SORT_SERIES.equals(elementName)) { this.setSort_Series(value); }
    else {
      sNewKeys.add(elementName);
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(this.getClass().getName());
    sb.append(":\n");
    if (this.getAlbum() != null) { sb.append("ALBUM"); sb.append(": "); sb.append(this.getAlbum()); sb.append("\n"); }
    if (this.getArtist() != null) { sb.append("ARTIST"); sb.append(": "); sb.append(this.getArtist()); sb.append("\n"); }
    if (this.getArtwork_Count() != null) { sb.append("ARTWORK_COUNT"); sb.append(": "); sb.append(this.getArtwork_Count()); sb.append("\n"); }
    if (this.getBit_Rate() != null) { sb.append("BIT_RATE"); sb.append(": "); sb.append(this.getBit_Rate()); sb.append("\n"); }
    if (this.getComments() != null) { sb.append("COMMENTS"); sb.append(": "); sb.append(this.getComments()); sb.append("\n"); }
    if (this.getCompilation() != null) { sb.append("COMPILATION"); sb.append(": "); sb.append(this.getCompilation()); sb.append("\n"); }
    if (this.getComposer() != null) { sb.append("COMPOSER"); sb.append(": "); sb.append(this.getComposer()); sb.append("\n"); }
    if (this.getDate_Added() != null) { sb.append("DATE_ADDED"); sb.append(": "); sb.append(this.getDate_Added()); sb.append("\n"); }
    if (this.getDate_Modified() != null) { sb.append("DATE_MODIFIED"); sb.append(": "); sb.append(this.getDate_Modified()); sb.append("\n"); }
    if (this.getDisc_Count() != null) { sb.append("DISC_COUNT"); sb.append(": "); sb.append(this.getDisc_Count()); sb.append("\n"); }
    if (this.getDisc_Number() != null) { sb.append("DISC_NUMBER"); sb.append(": "); sb.append(this.getDisc_Number()); sb.append("\n"); }
    if (this.getFile_Creator() != null) { sb.append("FILE_CREATOR"); sb.append(": "); sb.append(this.getFile_Creator()); sb.append("\n"); }
    if (this.getFile_Folder_Count() != null) { sb.append("FILE_FOLDER_COUNT"); sb.append(": "); sb.append(this.getFile_Folder_Count()); sb.append("\n"); }
    if (this.getFile_Type() != null) { sb.append("FILE_TYPE"); sb.append(": "); sb.append(this.getFile_Type()); sb.append("\n"); }
    if (this.getGenre() != null) { sb.append("GENRE"); sb.append(": "); sb.append(this.getGenre()); sb.append("\n"); }
    if (this.getKind() != null) { sb.append("KIND"); sb.append(": "); sb.append(this.getKind()); sb.append("\n"); }
    if (this.getLibrary_Folder_Count() != null) { sb.append("LIBRARY_FOLDER_COUNT"); sb.append(": "); sb.append(this.getLibrary_Folder_Count()); sb.append("\n"); }
    if (this.getLocation() != null) { sb.append("LOCATION"); sb.append(": "); sb.append(this.getLocation()); sb.append("\n"); }
    if (this.getName() != null) { sb.append("NAME"); sb.append(": "); sb.append(this.getName()); sb.append("\n"); }
    if (this.getPlay_Count() != null) { sb.append("PLAY_COUNT"); sb.append(": "); sb.append(this.getPlay_Count()); sb.append("\n"); }
    if (this.getPlay_Date() != null) { sb.append("PLAY_DATE"); sb.append(": "); sb.append(this.getPlay_Date()); sb.append("\n"); }
    if (this.getPlay_Date_UTC() != null) { sb.append("PLAY_DATE_UTC"); sb.append(": "); sb.append(this.getPlay_Date_UTC()); sb.append("\n"); }
    if (this.getSample_Rate() != null) { sb.append("SAMPLE_RATE"); sb.append(": "); sb.append(this.getSample_Rate()); sb.append("\n"); }
    if (this.getSize() != null) { sb.append("SIZE"); sb.append(": "); sb.append(this.getSize()); sb.append("\n"); }
    if (this.getTotal_Time() != null) { sb.append("TOTAL_TIME"); sb.append(": "); sb.append(this.getTotal_Time()); sb.append("\n"); }
    if (this.getTrack_Count() != null) { sb.append("TRACK_COUNT"); sb.append(": "); sb.append(this.getTrack_Count()); sb.append("\n"); }
    if (this.getTrack_ID() != null) { sb.append("TRACK_ID"); sb.append(": "); sb.append(this.getTrack_ID()); sb.append("\n"); }
    if (this.getTrack_Number() != null) { sb.append("TRACK_NUMBER"); sb.append(": "); sb.append(this.getTrack_Number()); sb.append("\n"); }
    if (this.getTrack_Type() != null) { sb.append("TRACK_TYPE"); sb.append(": "); sb.append(this.getTrack_Type()); sb.append("\n"); }
    if (this.getYear() != null) { sb.append("YEAR"); sb.append(": "); sb.append(this.getYear()); sb.append("\n"); }
    if (this.getSeason() != null) { sb.append("SEASON"); sb.append(": "); sb.append(this.getSeason()); sb.append("\n"); }
    if (this.getPersistent_ID() != null) { sb.append("PERSISTENT_ID"); sb.append(": "); sb.append(this.getPersistent_ID()); sb.append("\n"); }
    if (this.getSeries() != null) { sb.append("SERIES"); sb.append(": "); sb.append(this.getSeries()); sb.append("\n"); }
    if (this.getEpisode() != null) { sb.append("EPISODE"); sb.append(": "); sb.append(this.getEpisode()); sb.append("\n"); }
    if (this.getEpisode_Order() != null) { sb.append("EPISODE_ORDER"); sb.append(": "); sb.append(this.getEpisode_Order()); sb.append("\n"); }
    if (this.getHas_Video() != null) { sb.append("HAS_VIDEO"); sb.append(": "); sb.append(this.getHas_Video()); sb.append("\n"); }
    if (this.getTV_Show() != null) { sb.append("TV_SHOW"); sb.append(": "); sb.append(this.getTV_Show()); sb.append("\n"); }
    if (this.getProtected() != null) { sb.append("PROTECTED"); sb.append(": "); sb.append(this.getProtected()); sb.append("\n"); }
    if (this.getBPM() != null) { sb.append("BPM"); sb.append(": "); sb.append(this.getBPM()); sb.append("\n"); }
    if (this.getAlbum_Artist() != null) { sb.append("ALBUM_ARTIST"); sb.append(": "); sb.append(this.getAlbum_Artist()); sb.append("\n"); }
    if (this.getExplicit() != null) { sb.append("EXPLICIT"); sb.append(": "); sb.append(this.getExplicit()); sb.append("\n"); }
    if (this.getSkip_Count() != null) { sb.append("SKIP_COUNT"); sb.append(": "); sb.append(this.getSkip_Count()); sb.append("\n"); }
    if (this.getSkip_Date() != null) { sb.append("SKIP_DATE"); sb.append(": "); sb.append(this.getSkip_Date()); sb.append("\n"); }
    if (this.getRelease_Date() != null) { sb.append("RELEASE_DATE"); sb.append(": "); sb.append(this.getRelease_Date()); sb.append("\n"); }
    if (this.getPodcast() != null) { sb.append("PODCAST"); sb.append(": "); sb.append(this.getPodcast()); sb.append("\n"); }
    if (this.getMovie() != null) { sb.append("MOVIE"); sb.append(": "); sb.append(this.getMovie()); sb.append("\n"); }
    if (this.getUnplayed() != null) { sb.append("UNPLAYED"); sb.append(": "); sb.append(this.getUnplayed()); sb.append("\n"); }
    if (this.getSort_Album() != null) { sb.append("SORT_ALBUM"); sb.append(": "); sb.append(this.getSort_Album()); sb.append("\n"); }
    if (this.getSort_Album_Artist() != null) { sb.append("SORT_ALBUM_ARTIST"); sb.append(": "); sb.append(this.getSort_Album_Artist()); sb.append("\n"); }
    if (this.getSort_Artist() != null) { sb.append("SORT_ARTIST"); sb.append(": "); sb.append(this.getSort_Artist()); sb.append("\n"); }
    if (this.getSort_Composer() != null) { sb.append("SORT_COMPOSER"); sb.append(": "); sb.append(this.getSort_Composer()); sb.append("\n"); }
    if (this.getSort_Name() != null) { sb.append("SORT_NAME"); sb.append(": "); sb.append(this.getSort_Name()); sb.append("\n"); }
    if (this.getContent_Rating() != null) { sb.append("CONTENT_RATING"); sb.append(": "); sb.append(this.getContent_Rating()); sb.append("\n"); }
    if (this.getDisabled() != null) { sb.append("DISABLED"); sb.append(": "); sb.append(this.getDisabled()); sb.append("\n"); }
    if (this.getPurchased() != null) { sb.append("PURCHASED"); sb.append(": "); sb.append(this.getPurchased()); sb.append("\n"); }
    if (this.getVideo_Height() != null) { sb.append("VIDEO_HEIGHT"); sb.append(": "); sb.append(this.getVideo_Height()); sb.append("\n"); }
    if (this.getVideo_Width() != null) { sb.append("VIDEO_WIDTH"); sb.append(": "); sb.append(this.getVideo_Width()); sb.append("\n"); }
    if (this.getEID() != null) { sb.append("EID"); sb.append(": "); sb.append(this.getEID()); sb.append("\n"); }
    if (this.getHD() != null) { sb.append("HD"); sb.append(": "); sb.append(this.getHD()); sb.append("\n"); }
    if (this.getAlbum_Rating() != null) { sb.append("ALBUM_RATING"); sb.append(": "); sb.append(this.getAlbum_Rating()); sb.append("\n"); }
    if (this.getAlbum_Rating_Computed() != null) { sb.append("ALBUM_RATING_COMPUTED"); sb.append(": "); sb.append(this.getAlbum_Rating_Computed()); sb.append("\n"); }
    if (this.getRating() != null) { sb.append("RATING"); sb.append(": "); sb.append(this.getRating()); sb.append("\n"); }
    if (this.getGrouping() != null) { sb.append("GROUPING"); sb.append(": "); sb.append(this.getGrouping()); sb.append("\n"); }
    if (this.getPart_Of_Gapless_Album() != null) { sb.append("PART_OF_GAPLESS_ALBUM"); sb.append(": "); sb.append(this.getPart_Of_Gapless_Album()); sb.append("\n"); }
    if (this.getMusic_Video() != null) { sb.append("MUSIC_VIDEO"); sb.append(": "); sb.append(this.getMusic_Video()); sb.append("\n"); }
    if (this.getMatchID() != null) { sb.append("MATCH_ID"); sb.append(": "); sb.append(this.getMatchID()); sb.append("\n"); }
    if (this.getXID() != null) { sb.append("XID"); sb.append(": "); sb.append(this.getXID()); sb.append("\n"); }
    if (this.getRating_Computed() != null) { sb.append("RATING_COMPUTED"); sb.append(": "); sb.append(this.getRating_Computed()); sb.append("\n"); }
    if (this.getSort_Series() != null) { sb.append("SORT_SERIES"); sb.append(": "); sb.append(this.getSort_Series()); sb.append("\n"); }
    return sb.toString();
  }

  public boolean isAudioKind() { return sAudioKinds.contains(this.getKind()); }

  public static void report() {
    if (sNewKeys.size() > 0) {
      System.out.println("iTunes added new keys:");
      for (String key : sNewKeys) {
          String varName = key.toUpperCase().replaceAll(" ", "_").replaceAll("/", "_").replaceAll("-", "_");
          System.out.println("private static final String TK_" + varName + " = \"" + key + "\";");
      }
    }

    if (sNewKinds.size() > 0) {
      System.out.println("iTunes added new kinds:");
      for (String kind : sNewKinds) {
        String varName = kind.toUpperCase().replaceAll(" ", "_").replaceAll("/", "_").replaceAll("-", "_");
        System.out.println("private static final String TKIND_" + varName + " = \"" + kind + "\";");
      }
    }
  }
}
