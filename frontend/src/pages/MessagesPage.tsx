import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { MessageSquare, Send, User, Clock, CheckCheck, Box } from 'lucide-react';
import { chatService } from '../services/chatService';
import { ChatMessage, Conversation } from '../types';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const MessagesPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const { error } = useToast();

  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversation, setActiveConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputText, setInputText] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [sending, setSending] = useState<boolean>(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const initialConvId = searchParams.get('conversationId')
    ? Number(searchParams.get('conversationId'))
    : null;

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    chatService
      .getConversations(0, 50)
      .then((res) => {
        setConversations(res.content);
        if (initialConvId) {
          const match = res.content.find((c) => c.id === initialConvId);
          if (match) setActiveConversation(match);
        } else if (res.content.length > 0) {
          setActiveConversation(res.content[0]);
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [initialConvId]);

  useEffect(() => {
    if (!activeConversation) return;

    chatService
      .getMessages(activeConversation.id, 0, 100)
      .then((res) => {
        setMessages(res.content);
        scrollToBottom();
      })
      .catch(() => {});

    chatService.markAsRead(activeConversation.id).catch(() => {});
  }, [activeConversation]);

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeConversation || !inputText.trim()) return;

    const content = inputText.trim();
    setInputText('');
    setSending(true);

    try {
      const sentMsg = await chatService.sendMessage({
        conversationId: activeConversation.id,
        recipientId: activeConversation.otherUserId,
        content,
      });

      setMessages((prev) => [...prev, sentMsg]);
      scrollToBottom();
    } catch {
      error('Failed to send message.');
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 py-8">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-3xl font-extrabold text-white tracking-tight mb-6">Messages</h1>

        <div className="bg-slate-900/80 border border-slate-800 rounded-3xl overflow-hidden grid grid-cols-1 md:grid-cols-3 h-[600px] shadow-2xl">
          {/* Left: Conversation List */}
          <div className="border-r border-slate-800 flex flex-col bg-slate-950/50">
            <div className="p-4 border-b border-slate-800 text-xs font-bold text-slate-400 uppercase tracking-wider">
              Active Threads ({conversations.length})
            </div>

            <div className="flex-1 overflow-y-auto divide-y divide-slate-850">
              {loading ? (
                <div className="p-6 text-center text-slate-500 text-xs">Loading chats...</div>
              ) : conversations.length > 0 ? (
                conversations.map((conv) => (
                  <button
                    key={conv.id}
                    onClick={() => setActiveConversation(conv)}
                    className={`w-full p-4 text-left flex items-start gap-3 transition-colors ${
                      activeConversation?.id === conv.id
                        ? 'bg-slate-800/80 border-l-4 border-emerald-500'
                        : 'hover:bg-slate-900/60'
                    }`}
                  >
                    <div className="w-10 h-10 rounded-xl bg-slate-800 text-emerald-400 font-bold flex items-center justify-center text-sm shrink-0">
                      {conv.otherUserName?.charAt(0) || 'U'}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between text-xs mb-1">
                        <span className="font-semibold text-white truncate">{conv.otherUserName}</span>
                        {conv.lastMessageTime && (
                          <span className="text-[10px] text-slate-500">
                            {new Date(conv.lastMessageTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-slate-400 truncate">{conv.lastMessage || 'Conversation started'}</p>
                    </div>
                  </button>
                ))
              ) : (
                <div className="p-8 text-center text-slate-500 text-xs">No active conversations.</div>
              )}
            </div>
          </div>

          {/* Right: Messages View & Composer */}
          <div className="md:col-span-2 flex flex-col bg-slate-900/40">
            {activeConversation ? (
              <>
                {/* Header */}
                <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/70">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-emerald-950 text-emerald-400 border border-emerald-500/30 font-bold flex items-center justify-center text-sm">
                      {activeConversation.otherUserName?.charAt(0) || 'U'}
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-sm">{activeConversation.otherUserName}</h3>
                      <span className="text-[11px] text-emerald-400">Direct Message</span>
                    </div>
                  </div>
                </div>

                {/* Messages List */}
                <div className="flex-1 p-4 overflow-y-auto space-y-3">
                  {messages.map((msg) => {
                    const isMe = msg.senderId === user?.id;
                    return (
                      <div
                        key={msg.id}
                        className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}
                      >
                        <div
                          className={`max-w-md p-3.5 rounded-2xl text-xs leading-relaxed ${
                            isMe
                              ? 'bg-emerald-500 text-slate-950 font-medium rounded-tr-none shadow-md shadow-emerald-500/10'
                              : 'bg-slate-800 text-slate-200 rounded-tl-none border border-slate-700/60'
                          }`}
                        >
                          {msg.content}
                        </div>
                        <span className="text-[10px] text-slate-500 mt-1 px-1">
                          {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                    );
                  })}
                  <div ref={messagesEndRef} />
                </div>

                {/* Composer */}
                <form onSubmit={handleSendMessage} className="p-3 border-t border-slate-800 flex items-center gap-2 bg-slate-950/60">
                  <input
                    type="text"
                    placeholder="Type your message..."
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                    className="flex-1 px-4 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-emerald-500"
                  />
                  <button
                    type="submit"
                    disabled={sending || !inputText.trim()}
                    className="p-2.5 bg-emerald-500 hover:bg-emerald-400 disabled:opacity-40 text-slate-950 rounded-xl transition-all shadow-md"
                  >
                    <Send className="w-4 h-4" />
                  </button>
                </form>
              </>
            ) : (
              <div className="flex-1 flex flex-col items-center justify-center text-slate-500 text-xs p-6">
                <MessageSquare className="w-12 h-12 text-slate-700 mb-2" />
                Select a conversation thread on the left to start messaging.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
