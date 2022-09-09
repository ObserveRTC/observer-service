package org.observertc.observer.hamokendpoints.netty;

import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int readableBytes = in.readableBytes();
        if (readableBytes <= 0) {
            return;
        }

        String msg = in.toString(CharsetUtil.UTF_8);
        in.readerIndex(in.readerIndex() + in.readableBytes());

        Message message = new Message();

        out.add(message);

    }

}