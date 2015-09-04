require 'formula'

class Builder < Formula
  homepage 'https://github.com/hybris/builder-cli'
  head 'https://github.com/hybris/builder-cli'
  url 'file:///usr/local/Library/Taps/hybris/homebrew-builder/builder-sdk-cli/builder.tgz'
  version '2.1.2'
  sha1 '3306ccc9c200b1e88a0a1555a099bf300af477c3'
    
  depends_on :arch => :x86_64

  def install
      bin.install 'builder'
  end

  test do
    system "#{bin}/builder"
  end
end

