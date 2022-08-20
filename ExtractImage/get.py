from pytube import YouTube



def download_360p_mp4_videos(url: str, outpath: str = "./video"):

    yt = YouTube(url)

    yt.streams.filter(file_extension="mp4").get_by_resolution('720p').download(outpath)

if __name__ == "__main__":
    url = 'https://www.youtube.com/watch?v=rcVzEHNk-zc'
    download_360p_mp4_videos(url)