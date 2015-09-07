require 'formula'

class Builder < Formula
  homepage 'https://github.com/hybris/builder-cli'
  head 'https://github.com/hybris/builder-cli'
  url 'file:///usr/local/Library/Taps/hybris/builder-cli/homebrew-builder/builder.tgz'
  version '2.1.2'
  sha1 '303274fd60aec794c4451bdf354948f825a3b7d3'
    
  depends_on :arch => :x86_64

  def install
      bin.install 'builder'
  end

  test do
    system "#{bin}/builder"
  end
end

